package nz.co.searchwellington.urls;

import org.springframework.stereotype.Component;

@Component
public class FeedBurnerRedirectResolver extends AbstractRedirectResolver {
    
    public static final String FEEDBURNER_LINK_URL_PREFIX = "http://feedproxy.google.com/";
    
    public boolean isValid(String url) {
        return url != null && url.startsWith(FEEDBURNER_LINK_URL_PREFIX);
    }
    
}
