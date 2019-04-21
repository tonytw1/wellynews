package nz.co.searchwellington.utils;

public class HttpFetchResult {
	
	private final int status;
	private final String body;
	
	public HttpFetchResult(int status, String body) {
		this.status = status;
		this.body = body;
	}

	public int getStatus() {
		return status;
	}

	public String getBody() {
		return body;
	}

}
