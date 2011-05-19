package nz.co.searchwellington.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.sourceforge.jrobotx.util.URLInputStreamFactory;

import org.apache.commons.httpclient.HttpStatus;

 public class HttpClientInputStreamFactory implements URLInputStreamFactory {

	 StandardHttpFetcher httpFetcher;
	 	 	 
	 public HttpClientInputStreamFactory(StandardHttpFetcher httpFetcher) {
		 this.httpFetcher = httpFetcher;
	 }
	 
	 public InputStream openStream(URL url) throws IOException {
		 HttpFetchResult httpResult = httpFetcher.httpFetch(url.toString());
		 if (httpResult != null) {
			 if (httpResult.getStatus() == HttpStatus.SC_NOT_FOUND) {
				 return null;
			 }
			 return httpResult.getInputStream();
		 }
		 return null;
	 }
	 
}