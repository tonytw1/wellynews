package nz.co.searchwellington.twitter;

import nz.co.searchwellington.utils.AbstractRedirectResolverService;

import org.apache.log4j.Logger;

public class BitlyUrlResolverService extends AbstractRedirectResolverService {
    
    Logger log = Logger.getLogger(BitlyUrlResolverService.class);
    public static final String BITLY_URL_PREFIX = "http://bit.ly/";
    
    public boolean isValid(String url) {
        return url != null && url.startsWith(BITLY_URL_PREFIX);
    }
    
}
