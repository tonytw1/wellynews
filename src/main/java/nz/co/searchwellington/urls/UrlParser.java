package nz.co.searchwellington.urls;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.stereotype.Component;

@Component
public class UrlParser {

    public String extractHostnameFrom(String fullURL) {
        try {
            final URL url = new URL(fullURL);
            return new String(url.getHost());
            
        } catch (MalformedURLException e) {
            System.out.println(e);
        	return null;
        }        
    }
    
}
