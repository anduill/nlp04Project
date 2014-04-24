package nlp.lm;


import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static nlp.lm.BigramModel.*;

public class TestOriginalBigramModel {
    public static String atis_dir = "/Users/djchuy/development/nlp_old/data/pos/atis";
    public static String wsj_dir = "/Users/djchuy/development/nlp_old/data/pos/wsj";
    public static String brown_dir = "/Users/djchuy/development/nlp_old/data/pos/brown";
    public static String lyrics_dir = "/Users/djchuy/development/lyrics";
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
        System.out.println(testString2);
        System.out.println(testString3);
        System.out.println(testString5);

        String[] tokens = testString2.split("[.]");
        String token1 = tokens[0];
        String token2 = tokens[1].trim();
        System.out.println(token1.length());
        System.out.println(token2.length());
        System.out.println(testString2.contains("."));
    }

}
