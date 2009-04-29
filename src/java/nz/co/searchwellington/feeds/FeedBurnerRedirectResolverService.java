package nz.co.searchwellington.feeds;

import nz.co.searchwellington.utils.AbstractRedirectResolverService;

import org.apache.log4j.Logger;

public class FeedBurnerRedirectResolverService extends AbstractRedirectResolverService {
    
    Logger log = Logger.getLogger(FeedBurnerRedirectResolverService.class);
    public static final String FEEDBURNER_LINK_URL_PREFIX = "http://feedproxy.google.com/";
    
    public boolean isValid(String url) {
        return url != null && url.startsWith(FEEDBURNER_LINK_URL_PREFIX);
    }
    
}
