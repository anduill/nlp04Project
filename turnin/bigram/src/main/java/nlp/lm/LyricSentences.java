package nlp.lm;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;


public class LyricSentences implements Iterable<String> {
    private File _file;

    public LyricSentences(File file) {
        _file = file;
    }

    @Override
    public Iterator<String> iterator() {
        return new SentenceIterator(_file);
    }

    private class SentenceIterator implements Iterator<String> {
        private BufferedReader reader;
        private String nextSentence;
        private Queue<String> sentenceBuffer;
        private Boolean done = false;
        public SentenceIterator(File file) {
            try {
                reader = new BufferedReader(new FileReader(file));
                sentenceBuffer = Queues.newLinkedBlockingQueue();
                advanceBufferedReader();
            } catch (IOException e) {
                System.out.println("\nCould not open Lyric File: " + file);
                System.exit(1);
            }
        }

        @Override
        public boolean hasNext() {
            return !done;
        }

        @Override
        public String next() {
            String temp = nextSentence;
            try {
                advanceBufferedReader();
            } catch (IOException e) {
                System.out.println("Parsing error...last sentence parsed: "+temp);
                System.exit(1);
            }
            return temp;
        }

        private void advanceBufferedReader() throws IOException {
            if(!sentenceBuffer.isEmpty()){
                nextSentence = sentenceBuffer.poll();
            }
            else{
                Boolean goToNextLine = true;
                List<String> sentences = Lists.newArrayList();
                while(goToNextLine){
                    if(!sentences.isEmpty()){
                        goToNextLine = false;
                    }
                    else{
                        String line = reader.readLine();
                        if(line == null){
                            done = true;
                            goToNextLine = false;
                            reader.close();
                        }
                        else{
                            addToSentences(line,sentences);
                        }
                    }
                }
                for(String sentence: sentences){
                    sentenceBuffer.add(sentence);
                }
                nextSentence = sentenceBuffer.poll();
            }
        }

        private void addToSentences(String line, List<String> sentences) {
            String temp_line = line.replaceAll("\\[", "(");
            temp_line = temp_line.replaceAll("]",")");
            temp_line = temp_line.replaceAll("\\(.*\\)", " ");
            temp_line = temp_line.replaceAll("'","");
            temp_line = temp_line.replaceAll("[^\\w\\. ]+"," ");//remove all punctuation that is not a period or a space (retain words)
            String removedRedundantPeriods = temp_line.replaceAll("[.]+",".");
            if(removedRedundantPeriods.contains(".")){
                String[] tokens = removedRedundantPeriods.split("[.]");
                for(String token : tokens){
                    token = token.trim();
                    if(token.length() > 0){
                        sentences.add(finalSentenceCleaning(token));
                    }
                }
            }
            else{
                sentences.add(finalSentenceCleaning(removedRedundantPeriods));
            }
        }
        private String finalSentenceCleaning(String sentence){
            return sentence.toLowerCase().replaceAll("[^\\w ]+","").replaceAll("[ ]+"," ").trim();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove on lyrics iterator is not supported!");
        }
    }
}
