package nz.co.searchwellington.urls;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.stereotype.Component;

@Component
public class UrlParser {

    public String extractHostnameFrom(String fullURL) {
        System.out.println("@@@@ " + fullURL);
        try {
            final URL url = new URL(fullURL);
            System.out.println("!!! " + url);
            return new String(url.getHost());
            
        } catch (MalformedURLException e) {
            System.out.println(e);
        	return null;
        }        
    }
    
}
