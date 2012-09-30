package nz.co.searchwellington.urls;

import org.springframework.stereotype.Component;

@Component
public class TinyUrlResolver extends AbstractRedirectResolver {
	
    public static final String TINYURL_PREFIX = "http://tinyurl.com/";
    
    public boolean isValid(String url) {
        return url != null && url.startsWith(TINYURL_PREFIX);
    }
    
}
