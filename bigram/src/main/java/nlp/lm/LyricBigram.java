package nlp.lm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LyricBigram {
    /** Unigram model that maps a token to its unigram probability */
    public Map<String, DoubleValue> forwardUnigramMap = null;
    public Map<String, DoubleValue> backwardUnigramMap = null;
    /**  Bigram model that maps a bigram as a string "A\nB" to the
     *   P(B | A) */
    public Map<String, DoubleValue> forwardBigramMap = null;
    public Map<String, DoubleValue> backwardBigramMap = null;
    /** Total count of tokens in training data */
    public double tokenCount = 0;

    /** Interpolation weight for unigram model */
    public double unigramLambda1 = 0.1;
    public double bigramLambda = 0.45;

    /** Interpolation weight for bigram model */
    public double lambda2 = 0.9;
    public String beginSentenceTag = "<S>";
    public String endSentenceTag = "</S>";
    public String unknownToken = "<UNK>";

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

    public void trainBoth(List<List<String>> sentences){
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
        // First count an initial start sentence token
        String prevToken = beginTag;
        DoubleValue unigramValue = unigramMap.get(beginTag);
        unigramValue.increment();
        tokenCount++;
        // For each token in sentence, accumulate a unigram and bigram count
        for (String token : sequence) {
            unigramValue = unigramMap.get(token);
            // If this is the first time token is seen then count it
            // as an unkown token (<UNK>) to handle out-of-vocabulary
            // items in testing
            if (unigramValue == null) {
                // Store token in unigram map with 0 count to indicate that
                // token has been seen but not counted
                unigramMap.put(token, new DoubleValue());
                token = unknownToken;
                unigramValue = unigramMap.get(token);
            }
            unigramValue.increment();    // Count unigram
            tokenCount++;               // Count token
            // Make bigram string
            String bigram = bigram(prevToken, token);
            DoubleValue bigramValue = bigramMap.get(bigram);
            if (bigramValue == null) {
                // If previously unseen bigram, then
                // initialize it with a value
                bigramValue = new DoubleValue();
                bigramMap.put(bigram, bigramValue);
            }
            // Count bigram
            bigramValue.increment();
            prevToken = token;
        }
        // Account for end of sentence unigram
        unigramValue = unigramMap.get(endTag);
        unigramValue.increment();
        tokenCount++;
        // Account for end of sentence bigram
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
        // Set bigram values to conditional probability of second token given first
        for (Map.Entry<String, DoubleValue> entry : bigramMap.entrySet()) {
            // An entry in the HashMap maps a token to a DoubleValue
            String bigram = entry.getKey();
            // The value for the token is in the value of the DoubleValue
            DoubleValue value = entry.getValue();
            double bigramCount = value.getValue();
            String token1 = bigramToken1(bigram); // Get first token of bigram
            // Prob is ratio of bigram count to token1 unigram count
            double condProb = bigramCount / unigramMap.get(token1).getValue();
            // Set map value to conditional probability
            value.setValue(condProb);
        }
        // Store unigrams with zero count to remove from map
        List<String> zeroTokens = new ArrayList<String>();
        // Set unigram values to unigram probability
        for (Map.Entry<String, DoubleValue> entry : unigramMap.entrySet()) {
            // An entry in the HashMap maps a token to a DoubleValue
            String token = entry.getKey();
            // Uniggram count is the current map value
            DoubleValue value = entry.getValue();
            double count = value.getValue();
            if (count == 0)
                // If count is zero (due to first encounter as <UNK>)
                // then remove save it to remove from map
                zeroTokens.add(token);
            else
                // Set map value to prob of unigram
                value.setValue(count / tokenCount);
        }
        // Remove zero count unigrams from map
        for (String token : zeroTokens)
            unigramMap.remove(token);
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

    /** Print model as lists of unigram and bigram probabilities */
    public void print() {
        System.out.println("Unigram probs:");
        for (Map.Entry<String, DoubleValue> entry : forwardUnigramMap.entrySet()) {
            // An entry in the HashMap maps a token to a DoubleValue
            String token = entry.getKey();
            // The value for the token is in the value of the DoubleValue
            DoubleValue value = entry.getValue();
            System.out.println(token + " : " + value.getValue());
        }
        System.out.println("\nBigram probs:");
        for (Map.Entry<String, DoubleValue> entry : forwardBigramMap.entrySet()) {
            // An entry in the HashMap maps a token to a DoubleValue
            String bigram = entry.getKey();
            // The value for the token is in the value of the DoubleValue
            DoubleValue value = entry.getValue();
            System.out.println(bigramToken2(bigram) + " given " + bigramToken1(bigram) +
                    " : " + value.getValue());
        }
    }

    public double sequenceLogProb(List<String> sequence,
                                  Map<String, DoubleValue> unigramMap,
                                  Map<String, DoubleValue> bigramMap,
                                  String beginTag,
                                  String endTag){
        // Set start-sentence as initial token
        String prevToken = beginTag;
        // Maintain total sentence prob as sum of individual token
        // log probs (since adding logs is same as multiplying probs)
        double sentenceLogProb = 0;
        // Check prediction of each token in sentence
        for (String token : sequence) {
            // Retrieve unigram prob
            DoubleValue unigramVal = unigramMap.get(token);
            if (unigramVal == null) {
                // If token not in unigram model, treat as <UNK> token
                token = unknownToken;
                unigramVal = unigramMap.get(token);
            }
            // Get bigram prob
            String bigram = bigram(prevToken, token);
            DoubleValue bigramVal = bigramMap.get(bigram);
            // Compute log prob of token using interpolated prob of unigram and bigram
            double logProb = Math.log(interpolatedProb(unigramVal, bigramVal));
            // Add token log prob to sentence log prob
            sentenceLogProb += logProb;
            // update previous token and move to next token
            prevToken = token;
        }
        // Check prediction of end of sentence token
        DoubleValue unigramVal = unigramMap.get(endTag);
        String bigram = bigram(prevToken, endTag);
        DoubleValue bigramVal = bigramMap.get(bigram);
        double logProb = Math.log(interpolatedProb(unigramVal, bigramVal));
        // Update sentence log prob based on prediction of </S>
        sentenceLogProb += logProb;
        return sentenceLogProb;
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
        // In bigram unknown then its prob is zero
        if (bigramVal != null)
            bigramProb = bigramVal.getValue();
        // Linearly combine weighted unigram and bigram probs
        return unigramLambda1 * unigramVal.getValue() + lambda2 * bigramProb;
    }
    public double biInterpolatedProb(DoubleValue unigramVal, DoubleValue forwardBigramVal, DoubleValue backwardBigramVal) {
        double forwardBigramProb = forwardBigramVal != null ? forwardBigramVal.getValue() : 0;
        double backwardBigramProb = backwardBigramVal != null ? backwardBigramVal.getValue() : 0;
        return unigramLambda1 * unigramVal.getValue() + bigramLambda * forwardBigramProb + bigramLambda * backwardBigramProb;
    }

    public static int wordCount (List<List<String>> sentences) {
        int wordCount = 0;
        for (List<String> sentence : sentences) {
            wordCount += sentence.size();
        }
        return wordCount;
    }
}
