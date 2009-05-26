package nz.co.searchwellington.utils;

import java.io.InputStream;

public class HttpFetchResult {
	
	private int status;
	private InputStream inputStream;
	
	public HttpFetchResult(int status, InputStream inputStream) {
		this.status = status;
		this.inputStream = inputStream;
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
