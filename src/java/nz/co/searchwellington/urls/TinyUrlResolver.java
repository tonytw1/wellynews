package nz.co.searchwellington.urls;


import org.apache.log4j.Logger;

public class TinyUrlResolver extends AbstractRedirectResolver {
    
    Logger log = Logger.getLogger(TinyUrlResolver.class);
    public static final String TINYURL_PREFIX = "http://tinyurl.com/";
    
    public boolean isValid(String url) {
        return url != null && url.startsWith(TINYURL_PREFIX);
    }
    
}
