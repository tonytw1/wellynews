package nz.co.searchwellington.htmlparsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public abstract class ExtractorTestCase {

    final protected StringBuffer loadContent(File contentFile) throws IOException {
    	StringBuffer content = new StringBuffer();
    	Reader freader = new FileReader(contentFile);
    	BufferedReader in = new BufferedReader(freader);
    
    	String str;
    	while ((str = in.readLine()) != null) {
    		content.append(str);
    		content.append("\n");
    	}
    	in.close();
    	freader.close();
    	return content;
    }

}
