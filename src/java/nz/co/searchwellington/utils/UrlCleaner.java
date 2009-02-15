package nz.co.searchwellington.utils;

import org.apache.log4j.Logger;

public class UrlCleaner {

    Logger log = Logger.getLogger(UrlCleaner.class);

    private RedirectingUrlResolver[] redirectResolvers;

    public UrlCleaner(RedirectingUrlResolver... redirectResolvers) {
        this.redirectResolvers = redirectResolvers;
    }

    
    public String cleanSubmittedItemUrl(String url) {
        log.debug("Resolving redirects for url: " + url);

        for (RedirectingUrlResolver resolver : redirectResolvers) {
            if (resolver.isValid(url)) {
                String resolvedUrl = resolver.resolveUrl(url);
                if (resolvedUrl != null) {
                    log.info("Redirected url '" + url + "' resolved to: " + resolvedUrl);
                    url = resolvedUrl;
                } else {
                    log.warn("Failed to resolve redirected url: " + url);
                }

            }
            
        }        
        return filterSubmittedURL(url);
    }
    
  
    protected String filterSubmittedURL(String url) {
        url = UrlFilters.trimWhiteSpace(url);
        url = UrlFilters.stripPhpSession(url);
        url = UrlFilters.addHttpPrefixIfMissing(url);
        log.debug("cleaned url is: " + url);
        return url;
    }
    
}
