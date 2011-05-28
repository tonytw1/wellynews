package nz.co.searchwellington.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.sourceforge.jrobotx.util.URLInputStreamFactory;

import org.apache.commons.httpclient.HttpStatus;

public class HttpFetcherUrlInputStreamFactory implements URLInputStreamFactory {

	 private static final String EMPTY_STRING = "";
	 
	 private StandardHttpFetcher httpFetcher;
	 	 	 
	 public HttpFetcherUrlInputStreamFactory(StandardHttpFetcher httpFetcher) {
		 this.httpFetcher = httpFetcher;
	 }
	 
	 public InputStream openStream(URL url) throws IOException {
		 HttpFetchResult httpResult = httpFetcher.httpFetch(url.toString());
		 if (httpResult != null && httpResult.getStatus() == HttpStatus.SC_OK) {
			 return httpResult.getInputStream();			
		 }
		 return emptyInputStream();
	 }

	private InputStream emptyInputStream() {
		return new ByteArrayInputStream(EMPTY_STRING.getBytes());		
	}
	
}