package nz.co.searchwellington.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class HttpFetchResult {
	
	private final int status;
	private final InputStream inputStream;
	
	public HttpFetchResult(int status, InputStream inputStream) {
		this.status = status;
		this.inputStream = inputStream;
	}
	
    public String readEncodedResponse(String charSet) throws IOException {
    	return IOUtils.toString(this.inputStream);                 
    }
		
	public int getStatus() {
		return status;
	}	
	public InputStream getInputStream() {
		return inputStream;
	}
	
}
