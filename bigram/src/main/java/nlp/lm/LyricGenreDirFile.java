package nlp.lm;


import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LyricGenreDirFile implements SentenceTokenIzable {
    @Override
    public List<List<String>> convertToTokenLists(List<File> files) {
        List<List<String>> result = Lists.newArrayList();
        List<File> newFileList = getListOfLyricFiles(files);
        for(File file : newFileList){
            LyricSentences sentences = new LyricSentences(file);
            for(String sentence : sentences){
                if(sentence.length() > 0){
                    String[] tokens = sentence.split(" ");
                    result.add(Lists.newArrayList(tokens));
                }
            }
        }
        return result;
    }

    private List<File> getListOfLyricFiles(List<File> files) {
        List<File> result = Lists.newArrayList();
        for (File file : files) {
            getListHelper(result,file);
        }
        return result;
    }
    private void getListHelper(List<File> result, File aFile){
        if(!aFile.isDirectory()){
            result.add(aFile);
        }
        else{
            List<File> dirOfFiles = Lists.newArrayList(aFile.listFiles());
            for(File file : dirOfFiles){
                getListHelper(result, file);
            }
        }
    }
}
