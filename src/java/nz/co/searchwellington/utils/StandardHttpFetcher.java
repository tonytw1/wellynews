package nz.co.searchwellington.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.log4j.Logger;

public class StandardHttpFetcher implements HttpFetcher {

    Logger log = Logger.getLogger(StandardHttpFetcher.class);
    private static final int HTTP_TIMEOUT = 10000;
    
    private String userAgent;

    
    public HttpFetchResult httpFetch(String url) {
    	log.info("Attempting fetch of url: " + url);
		DefaultHttpClient client = setupClient();        
		
		try {
		    HttpGet method = new HttpGet(url);
		    HttpResponse response = client.execute(method);
            
		    log.info("http status was: " + response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream stream = response.getEntity().getContent();
				return new HttpFetchResult(response.getStatusLine().getStatusCode(), stream);
			}			
			return new HttpFetchResult(response.getStatusLine().getStatusCode(), null);
		
		} catch (ClientProtocolException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		return new HttpFetchResult(-1, null);
	}

    
	public String getUserAgent() {
		return userAgent;
	}

	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	private DefaultHttpClient setupClient() {
		DefaultHttpClient client = new DefaultHttpClient();
		if (userAgent != null) {
			client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);		
		}
		client.getParams().setParameter("http.socket.timeout", new Integer(HTTP_TIMEOUT));
		client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
		return client;
	}
	
}
