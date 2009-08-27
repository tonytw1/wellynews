package nz.co.searchwellington.utils;


import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public abstract class AbstractRedirectResolverService implements RedirectingUrlResolver {
    
    Logger log = Logger.getLogger(AbstractRedirectResolverService.class);        
    public static final int HTTP_TIMEOUT = 10000;
        
    public abstract boolean isValid(String url);
    
    public String resolveUrl(String url) {
        if (url != null && isValid(url)) {
            log.info("Resolving url: " + url);
           
            DefaultHttpClient client = new DefaultHttpClient();
            client.getParams().setParameter("http.socket.timeout", new Integer(HTTP_TIMEOUT));    
        
        	HttpHead method = new HttpHead(url);
        	//method.setFollowRedirects(false);
        	
			try {
				HttpResponse response = client.execute(method);
				
				final int statusCode = response.getStatusLine().getStatusCode();
				final boolean httpResponseWasRedirect = statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY;
				if (httpResponseWasRedirect) {
					Header locationHeader = response.getFirstHeader("Location");
					if (locationHeader != null) {
						log.info("Resolved to location: " + locationHeader.getValue());
						return locationHeader.getValue();
					}
				} else {
					log.warn("The http call did not return an expected redirect.");
				}	
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}           
          
        } else {
            log.warn("Url was invalid: " + null);
        }
        return null;
    }

}
