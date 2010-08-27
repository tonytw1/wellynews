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
		if (!url.isEmpty()) {
			url = UrlFilters.trimWhiteSpace(url);
			url = UrlFilters.addHttpPrefixIfMissing(url);
			return filterSubmittedURL(urlResolver.resolveUrl(url));		
		}
		log.warn("Called with an empty url");
		return url;
    }
    
    protected String filterSubmittedURL(String url) {
    	url = url.trim();
    	url = UrlFilters.stripFeedburnerParams(url);    	
        url = UrlFilters.trimWhiteSpace(url);
        url = UrlFilters.stripPhpSession(url);
        log.debug("Cleaned url is: " + url);
        return url;
    }
    
}
