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


}
