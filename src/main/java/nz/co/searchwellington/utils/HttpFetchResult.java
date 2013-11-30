package nz.co.searchwellington.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class HttpFetchResult {
	
	private int status;
	private InputStream inputStream;
	
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
	public void setStatus(int status) {
		this.status = status;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
}
