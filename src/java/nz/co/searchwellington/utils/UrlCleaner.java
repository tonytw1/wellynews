package nz.co.searchwellington.utils;

import nz.co.searchwellington.urls.UrlResolverService;

import org.apache.log4j.Logger;

public class UrlCleaner {

    Logger log = Logger.getLogger(UrlCleaner.class);

    private UrlResolverService urlResolver;
    
    public UrlCleaner(UrlResolverService urlResolver) {
		this.urlResolver = urlResolver;
	}

	public String cleanSubmittedItemUrl(String url) {        
        return filterSubmittedURL(urlResolver.resolveUrl(url));
    }
    
    protected String filterSubmittedURL(String url) {
        url = UrlFilters.trimWhiteSpace(url);
        url = UrlFilters.stripPhpSession(url);
        url = UrlFilters.addHttpPrefixIfMissing(url);
        log.debug("Cleaned url is: " + url);
        return url;
    }
    
}
