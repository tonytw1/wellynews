package nz.co.searchwellington.urls;


import org.apache.log4j.Logger;

public class BitlyUrlResolver extends AbstractRedirectResolver {
    
    Logger log = Logger.getLogger(BitlyUrlResolver.class);
    public static final String BITLY_URL_PREFIX = "http://bit.ly/";
    
    public boolean isValid(String url) {
        return url != null && url.startsWith(BITLY_URL_PREFIX);
    }
    
}
