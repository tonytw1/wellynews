package nz.co.searchwellington.utils;


import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

public abstract class AbstractRedirectResolverService implements RedirectingUrlResolver {
    
    Logger log = Logger.getLogger(AbstractRedirectResolverService.class);        
    public static final int HTTP_TIMEOUT = 10000;
        
    public abstract boolean isValid(String url);
    
    public String resolveUrl(String url) {
        if (url != null && isValid(url)) {
            log.info("Resolving url: " + url);
           
            HttpClient client = new HttpClient();
            client.getParams().setParameter("http.socket.timeout", new Integer(HTTP_TIMEOUT));    
            try {
            	HttpMethod method = new GetMethod(url);
            	method.setFollowRedirects(false);
                client.executeMethod(method);                   
                final boolean httpResponseWasRedirect = method.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY || method.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY;
                if (httpResponseWasRedirect) {
                    Header locationHeader = method.getResponseHeader("Location");
                    if (locationHeader != null) {
                        return locationHeader.getValue();
                    }
                } else {
                    log.warn("The http call did not return an expected redirect.");
                }
                
            } catch (HttpException e) {
                log.error(e);
            } catch (IOException e) {
                log.error(e);
            } catch (IllegalArgumentException e) {
            	log.error(e);
            }
          
        } else {
            log.warn("Url was invalid: " + null);
        }
        return null;
    }

}
