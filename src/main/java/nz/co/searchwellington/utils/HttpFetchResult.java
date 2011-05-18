package nz.co.searchwellington.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpFetchResult {
	
	private int status;
	private InputStream inputStream;
	
	public HttpFetchResult(int status, InputStream inputStream) {
		this.status = status;
		this.inputStream = inputStream;
	}
	
    public String readEncodedResponse(String charSet) throws IOException {
        BufferedReader d = new BufferedReader(new InputStreamReader(this.inputStream, charSet));        
        StringBuffer responseBody = new StringBuffer();
        String input;
        while ((input = d.readLine()) != null) {                
            responseBody.append(input);
            responseBody.append("\n");
        }
        return responseBody.toString();            
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
