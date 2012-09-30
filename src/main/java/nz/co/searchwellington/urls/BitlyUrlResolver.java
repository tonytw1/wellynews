package nz.co.searchwellington.urls;

import org.springframework.stereotype.Component;

@Component
public class BitlyUrlResolver extends AbstractRedirectResolver {
    
    public static final String BITLY_URL_PREFIX = "http://bit.ly/";
    
    public boolean isValid(String url) {
        return url != null && url.startsWith(BITLY_URL_PREFIX);
    }
    
}
