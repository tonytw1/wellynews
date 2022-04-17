package nz.co.searchwellington.urls;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.stereotype.Component;

@Component
public class UrlParser {

    public String extractHostnameFrom(String fullURL) {
        try {
            final URL url = new URL(fullURL);
            return url.getHost();
            
        } catch (MalformedURLException e) {
        	return null;
        }        
    }
    
}
