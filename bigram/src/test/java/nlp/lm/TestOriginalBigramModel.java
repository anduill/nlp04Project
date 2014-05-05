package nlp.lm;


import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static nlp.lm.BigramModel.*;

public class TestOriginalBigramModel {
    public static String atis_dir = "/Users/djchuy/development/nlp_old/data/pos/atis";
    public static String wsj_dir = "/Users/djchuy/development/nlp_old/data/pos/wsj";
    public static String brown_dir = "/Users/djchuy/development/nlp_old/data/pos/brown";
    public static String lyrics_dir = "/Users/djchuy/development/lyrics";
    public String lyricsBase = "/Users/djchuy/development/repositories/nlp04Project/data";

    public String electronic = lyricsBase+"/"+"electronic2";
    public String pop = lyricsBase+"/"+"pop";
    public String country = lyricsBase+"/"+"country";
    public String punk = lyricsBase+"/"+"punk";
    public String rock = lyricsBase+"/"+"rock";
    public String out = lyricsBase+"/"+"test_out";


    @Test
    public void testLyricsExtraction(){
        List<File> lyrics = Lists.newArrayList(new File(lyrics_dir));
        LyricGenreDirFile lyricsFile = new LyricGenreDirFile();
        List<List<String>> lyricSentences = lyricsFile.convertToTokenLists(lyrics);
        for(List<String> sentence : lyricSentences){
            System.out.println(sentence);
        }
    }

    @Test
    public void testOriginalBigram(){
        List<File> wsj = Lists.newArrayList(new File(wsj_dir));
        List<File> brown = Lists.newArrayList(new File(brown_dir));
        List<File> atis = Lists.newArrayList(new File(atis_dir));
        System.out.println("*******************Running ATIS**************\n");
        System.out.println("Running Forwards****************\n");
        runForwardBigram(atis, 0.1);
        System.out.println("Running Backwards***************\n");
        runBackwardBigram(atis,0.1);
        System.out.println("Running Bidirectional***********\n");
        runBidirectionalBigram(atis,0.1);

        System.out.println("*******************Running WSJ***************\n");
        System.out.println("Running Forwards****************\n");
        runForwardBigram(wsj, 0.1);
        System.out.println("Running Backwards***************\n");
        runBackwardBigram(wsj,0.1);
        System.out.println("Running Bidirectional***********\n");
        runBidirectionalBigram(wsj,0.1);

        System.out.println("*******************Running Brown*************\n");
        System.out.println("Running Forwards****************\n");
        runForwardBigram(brown, 0.1);
        System.out.println("Running Backwards***************\n");
        runBackwardBigram(brown,0.1);
        System.out.println("Running Bidirectional***********\n");
        runBidirectionalBigram(brown,0.1);
    }
    @Test
    public void testScrubing(){
        String testString1 = "I will always love you...I will love you...";
        String newString1 = testString1.replaceAll("[.]+",".");

        String testString2 = "I will[over here] always$#^ (this too) love;- [here is an example] you.  ";
        String testString3 = testString2.replaceAll("\\(.*\\)", "");
        String testString4 = testString3.replaceAll("\\[", "(");
        testString4 = testString4.replaceAll("]",")");
        testString4 = testString4.replaceAll("\\(.*\\)", "");
        String testString5 = testString3.replaceAll("[^\\w\\. ]+"," ");
        String testString6 = "What   am I saying  now      seriously?";
        System.out.println(Lists.newArrayList(testString6.replaceAll("[ ]+"," ").split(" ")));

        String[] tokens = testString2.split("[.]");
        String token1 = tokens[0];
        String token2 = tokens[1].trim();
    }
    @Test
    public void testStringOps() throws IOException {
        String[] args = {"-e",electronic,"-r",rock,"-p",pop,"-pu",punk,"-o",out,"-s","8","-sz","150"};
        GenreModels.main(args);
    }
    @Test
    public void testRandomNumbers(){
        Random random = new Random(8l);
        Double value = random.nextDouble();
        for(int i = 0; i < 10; i++){
//            random.setSeed(i);
            System.out.println(value);
            value = random.nextDouble();
        }
    }

}
