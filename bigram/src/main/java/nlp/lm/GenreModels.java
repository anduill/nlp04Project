package nlp.lm;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import com.sun.tools.javac.util.Pair;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.io.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GenreModels {
    private static LyricBigram electronic;
    private static LyricBigram pop;
    private static LyricBigram punk;
    private static LyricBigram rock;

    private static LyricGenreDirFile lyricTransformer = new LyricGenreDirFile();

    public static void main(String[] args) throws IOException {
        LyricOptionParser parser = new LyricOptionParser();
        Args.usage(parser);
        Args.parse(parser, args);

        File _electronic = new File(parser.electronic);
        File _pop = new File(parser.pop);
        File _country = new File(parser.punk);
        File _rock = new File(parser.rock);

        Pair<List<File>, List<File>> electPairFiles = extractTextJson(_electronic);
        Pair<List<File>, List<File>> popPairFiles = extractTextJson(_pop);
        Pair<List<File>, List<File>> punkPairFiles = extractTextJson(_country);
        Pair<List<File>, List<File>> rockPairFiles = extractTextJson(_rock);

        File _output = new File(parser.output);
        _output.mkdir();
        Long seed = parser.seed;
        Integer modelSampleSize = parser.size;
        Integer testingSize = parser.testingSize;

        List<File> trainingFiles = Lists.newArrayList();

        List<File> elecTestingFiles = Lists.newArrayList();
        List<File> elecJsonTestingFiles = Lists.newArrayList();
        populateTrainingTesting(electPairFiles, trainingFiles, elecTestingFiles, elecJsonTestingFiles, seed, modelSampleSize, testingSize);
        electronic = trainBigram(trainingFiles);
        refreshLists(trainingFiles);

        List<File> popTestingFiles = Lists.newArrayList();
        List<File> popJsonTestingFiles = Lists.newArrayList();
        populateTrainingTesting(popPairFiles, trainingFiles, popTestingFiles, popJsonTestingFiles, seed, modelSampleSize, testingSize);
        pop = trainBigram(trainingFiles);
        refreshLists(trainingFiles);

        List<File> punkTestingFiles = Lists.newArrayList();
        List<File> punkJsonTestingFiles = Lists.newArrayList();
        populateTrainingTesting(punkPairFiles, trainingFiles, punkTestingFiles, punkJsonTestingFiles, seed, modelSampleSize, testingSize);
        punk = trainBigram(trainingFiles);
        refreshLists(trainingFiles);

        List<File> rockTestingFiles = Lists.newArrayList();
        List<File> rockJsonTestingFiles = Lists.newArrayList();
        populateTrainingTesting(rockPairFiles, trainingFiles, rockTestingFiles, rockJsonTestingFiles, seed, modelSampleSize, testingSize);
        rock = trainBigram(trainingFiles);
        refreshLists(trainingFiles);

        List<File> allText = elecTestingFiles;
        allText.addAll(popTestingFiles);
        allText.addAll(punkTestingFiles);
        allText.addAll(rockTestingFiles);
        List<File> allJson = elecJsonTestingFiles;
        allJson.addAll(popJsonTestingFiles);
        allJson.addAll(punkJsonTestingFiles);
        allJson.addAll(rockJsonTestingFiles);
        appendFeatures(electronic, pop, punk, rock, allText, allJson, _output);
    }

    private static void appendFeatures(LyricBigram electronic,
                                       LyricBigram pop,
                                       LyricBigram punk,
                                       LyricBigram rock,
                                       List<File> allText,
                                       List<File> allJson,
                                       File ouputDir) throws IOException {
        Integer numRockClassified = 0;
        Integer numRockCorrect = 0;
        Integer numPopClassified = 0;
        Integer numPopCorrect = 0;
        Integer numPunkClassified = 0;
        Integer numPunkCorrect = 0;
        Integer numElecClassified = 0;
        Integer numElecCorrect = 0;
        Map<String, Double> klMap = Maps.newHashMap();
        for (int i = 0; i < allText.size(); i++) {
            File textFile = allText.get(i);
            File jsonFile = allJson.get(i);

            List<List<String>> songLyricSentences = lyricTransformer.convertToTokenLists(Lists.newArrayList(textFile));
            Double[] lang_vector = new Double[4];
            lang_vector[0] = electronic.bidirectionalComplex(songLyricSentences);
            lang_vector[1] = pop.bidirectionalComplex(songLyricSentences);
            lang_vector[2] = punk.bidirectionalComplex(songLyricSentences);
            lang_vector[3] = rock.bidirectionalComplex(songLyricSentences);
            addKLEntry(klMap, "electronic-pop", electronic, pop, songLyricSentences);
            addKLEntry(klMap, "electronic-punk", electronic, punk, songLyricSentences);
            addKLEntry(klMap, "electronic-rock", electronic, rock, songLyricSentences);
            addKLEntry(klMap, "pop-punk", pop, punk, songLyricSentences);
            addKLEntry(klMap, "pop-rock", pop, rock, songLyricSentences);
            addKLEntry(klMap, "pop-electronic", pop, electronic, songLyricSentences);
            addKLEntry(klMap, "rock-punk", rock, punk, songLyricSentences);
            addKLEntry(klMap, "rock-pop", rock, pop, songLyricSentences);
            addKLEntry(klMap, "rock-electronic", rock, electronic, songLyricSentences);
            addKLEntry(klMap, "punk-rock", punk, rock, songLyricSentences);
            addKLEntry(klMap, "punk-pop", punk, pop, songLyricSentences);
            addKLEntry(klMap, "punk-electronic", punk, electronic, songLyricSentences);

            JSONObject json = (JSONObject) JSONValue.parse(new FileReader(jsonFile));
            json.put("lang_vector", lang_vector);
            if(lang_vector[3] <= lang_vector[2] && lang_vector[3] <= lang_vector[1] && lang_vector[3] <= lang_vector[0]){
                numRockClassified++;
                if(jsonFile.getAbsolutePath().contains("rock")){
                    numRockCorrect++;
                }
            }
            if(lang_vector[2] <= lang_vector[3] && lang_vector[2] <= lang_vector[1] && lang_vector[2] <= lang_vector[0]){
                numPunkClassified++;
                if(jsonFile.getAbsolutePath().contains("punk")){
                    numPunkCorrect++;
                }
            }
            if(lang_vector[1] <= lang_vector[2] && lang_vector[1] <= lang_vector[0] && lang_vector[1] <= lang_vector[3]){
                numPopClassified++;
                if(jsonFile.getAbsolutePath().contains("pop")){
                    numPopCorrect++;
                }
            }
            if(lang_vector[0] <= lang_vector[1] && lang_vector[0] <= lang_vector[2] && lang_vector[0] <= lang_vector[3]){
                numElecClassified++;
                if(jsonFile.getAbsolutePath().contains("electronic")){
                    numElecCorrect++;
                }
            }
            PrintWriter jsonWriter = new PrintWriter(ouputDir.getAbsolutePath() + "/" + jsonFile.getName());
            jsonWriter.println(json.toJSONString());
            jsonWriter.close();
        }
        for (Map.Entry<String, Double> klEntry : klMap.entrySet()) {
            System.out.println(klEntry.getKey() + " = " + klEntry.getValue());
        }
        System.out.println("\n");
        System.out.println("Number of Rock Classed: "+numRockClassified+" ... Number of Rock Correct: "+ numRockCorrect);
        System.out.println("Number of Punk Classed: "+numPunkClassified+" ... Number of Punk Correct: "+ numPunkCorrect);
        System.out.println("Number of Pop Classed: "+numPopClassified+" ... Number of Pop Correct: "+ numPopCorrect);
        System.out.println("Number of Electronic Classed: "+numElecClassified+" ... Number of Electronic Correct: "+ numElecCorrect);
    }

    private static void addKLEntry(Map<String, Double> klMap,
                                   String label,
                                   LyricBigram p_dist,
                                   LyricBigram q_dist,
                                   List<List<String>> songLyricSentences) {
        Double klSentencesTermsSum = p_dist.sentencesSumOfKLTerms(songLyricSentences, q_dist);
        if (klMap.containsKey(label)) {
            klMap.put(label, klMap.get(label) + klSentencesTermsSum);
        } else {
            klMap.put(label, klSentencesTermsSum);
        }
    }

    private static void refreshLists(List<File>... lists) {
        for (List<File> list : lists) {
            list.clear();
        }
    }

    private static LyricBigram trainBigram(List<File> trainingFiles) {
        List<List<String>> trainingSentences = lyricTransformer.convertToTokenLists(trainingFiles);
        LyricBigram bigram = new LyricBigram();
        bigram.trainBidirectional(trainingSentences);
        return bigram;
    }

    private static void populateTrainingTesting(Pair<List<File>, List<File>> textAndJson,
                                                List<File> trainingFiles,
                                                List<File> testingFiles,
                                                List<File> jsonTestingFiles,
                                                Long seed,
                                                Integer trainingSize,
                                                Integer testingSize) {
        List<File> textFiles = textAndJson.fst;
        List<File> jsonFiles = textAndJson.snd;
        List<Integer> trainingIndices = Lists.newArrayList();
        List<Integer> testingIndices = Lists.newArrayList();
        Integer totalNumberOfFiles = textFiles.size();
        for (int i = 0; i < totalNumberOfFiles; i++) {
            trainingIndices.add(i);
        }
        Boolean done = false;
        Random random = new Random(seed);
        while (!done) {
            if (testingIndices.size() >= testingSize) {
                done = true;
            } else {
                int nextIndex = random.nextInt(trainingIndices.size());

                Integer lookedUpIndex = trainingIndices.get(nextIndex);
                testingIndices.add(lookedUpIndex);
                trainingIndices.remove(nextIndex);

            }
        }
        if (trainingIndices.size() > trainingSize) {
            done = false;
            while(!done){
                if(trainingIndices.size() <= trainingSize){
                    done = true;
                }
                else{
                    int nextIndex = random.nextInt(trainingIndices.size());
                    trainingIndices.remove(nextIndex);
                }
            }
        }
        for (Integer i : trainingIndices) {
            trainingFiles.add(textFiles.get(i));
        }
        for (Integer i : testingIndices) {
            testingFiles.add(textFiles.get(i));
            jsonTestingFiles.add(jsonFiles.get(i));
        }
    }

    private static Pair<List<File>, List<File>> extractTextJson(File directory) {
        List<File> jsonFiles = Lists.newArrayList();
        List<File> txtFiles = Lists.newArrayList();
        for (File file : directory.listFiles()) {
            String fileName = file.getAbsolutePath();
            if (fileName.endsWith(".txt")) {
                String filePrefix = extractPrefix(fileName, ".txt");
                String jsonFileName = filePrefix + ".json";
                File jsonFile = new File(jsonFileName);
                if (jsonFile.exists()) {
                    jsonFiles.add(jsonFile);
                    txtFiles.add(file);
                }
            }
        }
        return new Pair<List<File>, List<File>>(txtFiles, jsonFiles);
    }

    private static String extractPrefix(String fileName, String s) {
        return fileName.substring(0, fileName.indexOf(s));
    }

    public static class LyricOptionParser {
        @Argument(value = "electronic", alias = "e", required = true, description = "Complete path to electronic dataset directory")
        private String electronic;
        @Argument(value = "pop", alias = "p", required = true, description = "Complete path to pop dataset directory")
        private String pop;
        @Argument(value = "punk", alias = "pu", required = true, description = "Complete path to punk dataset directory")
        private String punk;
        @Argument(value = "rock", alias = "r", required = true, description = "Complete path to rock dataset directory")
        private String rock;
        @Argument(value = "outputDir", alias = "o", required = true, description = "Output directory directory")
        private String output;
        @Argument(value = "seed", alias = "s", required = true, description = "Seed value for sampling")
        private Long seed;
        @Argument(value = "size", alias = "sz", required = true, description = "Number of lyric samples for building  genre bigrams")
        private Integer size;
        @Argument(value = "testingSize", alias = "t", required = true, description = "Number of points for testing")
        private Integer testingSize;
    }
}
