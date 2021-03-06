package nlp.lm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jidesoft.utils.BigDecimalMathUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LyricBigram {
    /** Unigram model that maps a token to its unigram probability */
    private Map<String, DoubleValue> forwardUnigramMap = null;
    private Map<String, DoubleValue> backwardUnigramMap = null;
    /**  Bigram model that maps a bigram as a string "A\nB" to the
     *   P(B | A) */
    private Map<String, DoubleValue> forwardBigramMap = null;
    private Map<String, DoubleValue> backwardBigramMap = null;
    /** Total count of tokens in training data */
    private double tokenCount = 0;

    /** Interpolation weight for unigram model */
    private double unigramLambda1 = 0.1;
    private double bigramLambda = 0.45;

    /** Interpolation weight for bigram model */
    private double lambda2 = 0.9;
    private String beginSentenceTag = "<S>";
    private String endSentenceTag = "</S>";
    private String unknownToken = "<UNK>";

    /** Initialize model with empty hashmaps with initial
     *  unigram entries for setence start (<S>), sentence end (</S>)
     *  and unknown tokens */
    public LyricBigram() {
        forwardUnigramMap = Maps.newHashMap();
        forwardBigramMap = Maps.newHashMap();
        backwardUnigramMap = Maps.newHashMap();
        backwardBigramMap = Maps.newHashMap();

        forwardUnigramMap.put(beginSentenceTag, new DoubleValue());
        forwardUnigramMap.put(endSentenceTag, new DoubleValue());
        forwardUnigramMap.put(unknownToken, new DoubleValue());
        backwardUnigramMap.put(beginSentenceTag,new DoubleValue());
        backwardUnigramMap.put(endSentenceTag,new DoubleValue());
        backwardUnigramMap.put(unknownToken,new DoubleValue());
    }

    private List<List<String>> reverseEachSentence(List<List<String>> sentences){
        List<List<String>> result = Lists.newArrayList();
        for(List<String> sentence : sentences){
            result.add(Lists.reverse(sentence));
        }
        return result;
    }

    public void trainBidirectional(List<List<String>> sentences){
        for(List<String> sentence : sentences){
            trainSequence(sentence,forwardUnigramMap,forwardBigramMap,beginSentenceTag,endSentenceTag);
        }
        calculateProbs(forwardUnigramMap,forwardBigramMap);
        tokenCount = 0;
        List<List<String>> reversedSentences = reverseEachSentence(sentences);
        for(List<String> sentence : reversedSentences){
            trainSequence(sentence,backwardUnigramMap,backwardBigramMap,endSentenceTag,beginSentenceTag);
        }
        calculateProbs(backwardUnigramMap,backwardBigramMap);
    }


    public void trainSequence(List<String> sequence,
                              Map<String, DoubleValue> unigramMap,
                              Map<String, DoubleValue> bigramMap,
                              String beginTag,
                              String endTag){
        String prevToken = beginTag;
        DoubleValue unigramValue = unigramMap.get(beginTag);
        unigramValue.increment();
        tokenCount++;

        for (String token : sequence) {
            unigramValue = unigramMap.get(token);

            if (unigramValue == null) {

                unigramMap.put(token, new DoubleValue());
                token = unknownToken;
                unigramValue = unigramMap.get(token);
            }
            unigramValue.increment();
            tokenCount++;

            String bigram = bigram(prevToken, token);
            DoubleValue bigramValue = bigramMap.get(bigram);
            if (bigramValue == null) {

                bigramValue = new DoubleValue();
                bigramMap.put(bigram, bigramValue);
            }

            bigramValue.increment();
            prevToken = token;
        }

        unigramValue = unigramMap.get(endTag);
        unigramValue.increment();
        tokenCount++;

        String bigram = bigram(prevToken, endTag);
        DoubleValue bigramValue = bigramMap.get(bigram);
        if (bigramValue == null) {
            bigramValue = new DoubleValue();
            bigramMap.put(bigram, bigramValue);
        }
        bigramValue.increment();
    }

    /** Compute unigram and bigram probabilities from unigram and bigram counts */
    public void calculateProbs(Map<String, DoubleValue> unigramMap, Map<String, DoubleValue> bigramMap) {

        for (Map.Entry<String, DoubleValue> entry : bigramMap.entrySet()) {
            String bigram = entry.getKey();

            DoubleValue value = entry.getValue();
            double bigramCount = value.getValue();
            String token1 = bigramToken1(bigram);

            double condProb = bigramCount / unigramMap.get(token1).getValue();
            value.setValue(condProb);
        }
        List<String> zeroTokens = new ArrayList<String>();
        for (Map.Entry<String, DoubleValue> entry : unigramMap.entrySet()) {
            String token = entry.getKey();
            DoubleValue value = entry.getValue();
            double count = value.getValue();
            if (count == 0){
                zeroTokens.add(token);
            }
            else{
                value.setValue(count / tokenCount);
            }
        }
        for (String token : zeroTokens){
            unigramMap.remove(token);
        }
    }

    /** Return bigram string as two tokens separated by a newline */
    public String bigram (String prevToken, String token) {
        return prevToken + "\n" + token;
    }

    /** Return fist token of bigram (substring before newline) */
    public String bigramToken1 (String bigram) {
        int newlinePos = bigram.indexOf("\n");
        return bigram.substring(0,newlinePos);
    }

    /** Return second token of bigram (substring after newline) */
    public String bigramToken2 (String bigram) {
        int newlinePos = bigram.indexOf("\n");
        return bigram.substring(newlinePos + 1, bigram.length());
    }


    public Double bidirectionalComplex(List<List<String>> sentences){
        double totalLogProb = 0;
        double totalNumTokens = 0;
        for(List<String> sentence : sentences){
            totalNumTokens += sentence.size();
            double sentenceLogProb = bidirectionLogProb2(sentence,forwardUnigramMap,forwardBigramMap,
                    backwardBigramMap,beginSentenceTag,endSentenceTag);
            totalLogProb += sentenceLogProb;
        }
        double perplexity = Math.exp(-totalLogProb /totalNumTokens);
        return perplexity;
    }

    public Double sentencesSumOfKLTerms(List<List<String>> songLyricSentences, LyricBigram q) {
        Double sentencesKLSumTerms = 0.0;
        for(List<String> sentence :songLyricSentences){
            Double relativeEntropyDiff = getSentenceKLSumDiff(sentence, forwardUnigramMap, forwardBigramMap,
                    backwardBigramMap, beginSentenceTag, endSentenceTag, q);
            sentencesKLSumTerms += relativeEntropyDiff;
        }

        return sentencesKLSumTerms;
    }

    private Double getSentenceKLSumDiff(List<String> sentence,
                                             Map<String, DoubleValue> forwardUnigramMap,
                                             Map<String, DoubleValue> forwardBigramMap,
                                             Map<String, DoubleValue> backwardBigramMap,
                                             String beginSentenceTag,
                                             String endSentenceTag,
                                             LyricBigram q) {
        double sentenceKLSum = 0.0;
        String prevToken = beginSentenceTag;
        String followingToken = sentence.size() > 1 ? sentence.get(1) : endSentenceTag;
        for(int i = 0; i < sentence.size(); i++){
            String token = sentence.get(i);

            DoubleValue unigramVal = forwardUnigramMap.get(token);
            DoubleValue qUnigramVal = q.forwardUnigramMap.get(token);
            if(qUnigramVal == null){
                token = unknownToken;
                qUnigramVal = q.forwardUnigramMap.get(token);
            }
            if(unigramVal == null){
                token = unknownToken;
                unigramVal = forwardUnigramMap.get(token);
            }
            String fowardBigram = bigram(prevToken,token);
            String qForwardBigram = q.bigram(prevToken,token);
            String backwardBigram = bigram(followingToken,token);
            String qBackwardBigram = q.bigram(followingToken,token);
            DoubleValue forwardBigramVal = forwardBigramMap.get(fowardBigram);
            DoubleValue qForwardBigramVal = q.forwardBigramMap.get(qForwardBigram);
            DoubleValue backwardBigramVal = backwardBigramMap.get(backwardBigram);
            DoubleValue qBackwardBigramVal = q.backwardBigramMap.get(qBackwardBigram);

            double tokenBiInterpolatedProb = biInterpolatedProb(unigramVal,forwardBigramVal,backwardBigramVal);
            double qTokenBiInterpolatedProb = q.biInterpolatedProb(qUnigramVal,qForwardBigramVal,qBackwardBigramVal);
            double logProb = Math.log(tokenBiInterpolatedProb);
            double qLogProb = Math.log(qTokenBiInterpolatedProb);
            sentenceKLSum += tokenBiInterpolatedProb * logProb - tokenBiInterpolatedProb * qLogProb;

            prevToken = token;
            followingToken = i < sentence.size()-2 ? sentence.get(i+2) : endSentenceTag;
        }
        return sentenceKLSum;
    }

    private double bidirectionLogProb2(List<String> sentence,
                                       Map<String, DoubleValue> forwardUnigramMap,
                                       Map<String, DoubleValue> forwardBigramMap,
                                       Map<String, DoubleValue> backwardBigramMap,
                                       String beginSentenceTag, String endSentenceTag) {
        String prevToken = beginSentenceTag;
        String followingToken = sentence.size() > 1 ? sentence.get(1) : endSentenceTag;
        double sentenceLogProb = 0;
        for(int i = 0; i < sentence.size(); i++){
            String token = sentence.get(i);
            DoubleValue unigramVal = forwardUnigramMap.get(token);
            if(unigramVal == null){
                token = unknownToken;
                unigramVal = forwardUnigramMap.get(token);
            }
            String fowardBigram = bigram(prevToken,token);
            String backwardBigram = bigram(followingToken,token);
            DoubleValue forwardBigramVal = forwardBigramMap.get(fowardBigram);
            DoubleValue backwardBigramVal = backwardBigramMap.get(backwardBigram);
            double logProb = Math.log(biInterpolatedProb(unigramVal,forwardBigramVal,backwardBigramVal));
            sentenceLogProb += logProb;

            prevToken = token;
            followingToken = i < sentence.size()-2 ? sentence.get(i+2) : endSentenceTag;
        }
        return sentenceLogProb;
    }

    /** Interpolate bigram prob using bigram and unigram model predictions */
    public double interpolatedProb(DoubleValue unigramVal, DoubleValue bigramVal) {
        double bigramProb = 0;
        if (bigramVal != null){
            bigramProb = bigramVal.getValue();
        }
        return unigramLambda1 * unigramVal.getValue() + lambda2 * bigramProb;
    }
    public double biInterpolatedProb(DoubleValue unigramVal, DoubleValue forwardBigramVal, DoubleValue backwardBigramVal) {
        double forwardBigramProb = forwardBigramVal != null ? forwardBigramVal.getValue() : 0;
        double backwardBigramProb = backwardBigramVal != null ? backwardBigramVal.getValue() : 0;
        double result = unigramLambda1 * unigramVal.getValue() + bigramLambda * forwardBigramProb + bigramLambda * backwardBigramProb;
        return result;
    }

    public static int wordCount (List<List<String>> sentences) {
        int wordCount = 0;
        for (List<String> sentence : sentences) {
            wordCount += sentence.size();
        }
        return wordCount;
    }
}
