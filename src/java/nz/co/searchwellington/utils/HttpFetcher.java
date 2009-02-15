package nz.co.searchwellington.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

public class HttpFetcher {

    Logger log = Logger.getLogger(HttpFetcher.class);

	private static final int HTTP_TIMEOUT = 10000;

    public InputStream httpFetch(String url) {
		HttpClient client = new HttpClient();
        
        client.getParams().setParameter("http.socket.timeout", new Integer(HTTP_TIMEOUT));
        client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        
		log.info("Attempting fetch of url: " + url);
		try {
		    HttpMethod method = new GetMethod(url);
			client.executeMethod(method);

			// Record the snapshot.
            log.info("http status was: " + method.getStatusCode());
			if (method.getStatusCode() == HttpStatus.SC_OK) {
				return method.getResponseBodyAsStream();               
			}
            return null;

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
		return null;
	}

}
