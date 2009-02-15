package nz.co.searchwellington.repositories;

import java.io.*;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.*;

public class LuceneAnalyzer extends Analyzer {
    
    public final TokenStream tokenStream(String fieldName, Reader reader) {    
        TokenStream myTokens = new PorterStemFilter(new LowerCaseTokenizer(reader));
        return myTokens;
    }
    
}