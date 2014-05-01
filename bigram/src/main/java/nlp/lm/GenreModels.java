package nlp.lm;


import com.google.common.collect.Lists;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import com.sun.tools.javac.util.Pair;

import java.io.File;
import java.util.List;

public class GenreModels {
    private LyricBigram electronic;
    private LyricBigram pop;
    private LyricBigram punk;
    private LyricBigram rock;

    public static void main(String[] args){
        LyricOptionParser parser = new LyricOptionParser();
        Args.usage(parser);
        Args.parse(parser,args);

        File _electronic = new File(parser.electronic);
        File _pop = new File(parser.pop);
        File _country = new File(parser.punk);
        File _rock = new File(parser.rock);

        Pair<List<File>,List<File>> electPairFiles = extractTextJson(_electronic);
        Pair<List<File>,List<File>> popPairFiles = extractTextJson(_pop);
        Pair<List<File>,List<File>> punkPairFiles = extractTextJson(_country);
        Pair<List<File>,List<File>> rockPairFiles = extractTextJson(_rock);

        File _output = new File(parser.output);
        //_output.mkdir();
    }

    private static Pair<List<File>, List<File>> extractTextJson(File directory) {
        List<File> jsonFiles = Lists.newArrayList();
        List<File> txtFiles = Lists.newArrayList();
        for(File file : directory.listFiles()){
            String fileName = file.getAbsolutePath();
            if(fileName.endsWith(".txt")){
                String filePrefix = extractPrefix(fileName,".txt");
                String jsonFileName = filePrefix+".json";
                File jsonFile = new File(jsonFileName);
                if(jsonFile.exists()){
                    jsonFiles.add(jsonFile);
                    txtFiles.add(file);
                }
            }
        }
        return new Pair<List<File>, List<File>>(txtFiles,jsonFiles);
    }

    private static String extractPrefix(String fileName, String s) {
        return fileName.substring(0,fileName.indexOf(s));
    }

    public static class LyricOptionParser{
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
    }
}
