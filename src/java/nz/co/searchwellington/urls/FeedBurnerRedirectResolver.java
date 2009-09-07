package nz.co.searchwellington.urls;


import org.apache.log4j.Logger;

public class FeedBurnerRedirectResolver extends AbstractRedirectResolver {
    
    Logger log = Logger.getLogger(FeedBurnerRedirectResolver.class);
    public static final String FEEDBURNER_LINK_URL_PREFIX = "http://feedproxy.google.com/";
    
    public boolean isValid(String url) {
        return url != null && url.startsWith(FEEDBURNER_LINK_URL_PREFIX);
    }
    
}
