package nz.co.searchwellington.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;

public class StandardHttpFetcher implements HttpFetcher {

    Logger log = Logger.getLogger(StandardHttpFetcher.class);
    private static final int HTTP_TIMEOUT = 10000;
    
    private String userAgent;

    
    public HttpFetchResult httpFetch(String url) {
    	log.info("Attempting fetch of url: " + url);
		HttpClient client = setupClient();        
		try {
		    HttpMethod method = new GetMethod(url);
			client.executeMethod(method);
            log.info("http status was: " + method.getStatusCode());
			if (method.getStatusCode() == HttpStatus.SC_OK) {
				InputStream stream = method.getResponseBodyAsStream();
				return new HttpFetchResult(method.getStatusCode(), stream);
			}
			return new HttpFetchResult(method.getStatusCode(), null);
			
		} catch (HttpException e) {
		    log.warn("An exception was thrown will trying to http fetch; see debug log level");
            log.debug(e);
		} catch (IOException e) {
            log.warn("An exception was thrown will trying to http fetch; see debug log level");
            log.debug(e);		
		} catch (IllegalStateException e) {
            log.warn("An exception was thrown will trying to http fetch; see debug log level");
            log.debug(e);		        
        }
		return new HttpFetchResult(-1, null);
	}

    
	public String getUserAgent() {
		return userAgent;
	}

	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	private HttpClient setupClient() {
		HttpClient client = new HttpClient();
		if (userAgent != null) {
			client.getParams().setParameter(HttpClientParams.USER_AGENT, userAgent);
		}
		client.getParams().setParameter("http.socket.timeout", new Integer(HTTP_TIMEOUT));
		client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
		return client;
	}
	
}
