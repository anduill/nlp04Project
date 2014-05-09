package nlp.lm;


import java.io.File;
import java.util.List;

public interface SentenceTokenIzable {
    public List<List<String>> convertToTokenLists(List<File> files);
}
