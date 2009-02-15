package nz.co.searchwellington.twitter;

import nz.co.searchwellington.utils.AbstractRedirectResolverService;

import org.apache.log4j.Logger;

public class TinyUrlResolverService extends AbstractRedirectResolverService {
    
    Logger log = Logger.getLogger(TinyUrlResolverService.class);
    public static final String TINYURL_PREFIX = "http://tinyurl.com/";
    
    public boolean isValid(String url) {
        return url != null && url.startsWith(TINYURL_PREFIX);
    }
    
}
