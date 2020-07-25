package nz.co.searchwellington.urls;

import nz.co.searchwellington.urls.shorturls.CachingShortUrlResolverService;

import nz.co.searchwellington.utils.UrlFilters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UrlCleaner {

    Logger log = Logger.getLogger(UrlCleaner.class);

    private CachingShortUrlResolverService shortUrlResolver;

    @Autowired
    public UrlCleaner(CachingShortUrlResolverService shortUrlResolver) {
        this.shortUrlResolver = shortUrlResolver;
    }

    public String cleanSubmittedItemUrl(String url) {
        if (!url.isEmpty()) {
            // Trim and add prefix is missing from user submitted input
            url = UrlFilters.trimWhiteSpace(url);
            url = UrlFilters.addHttpPrefixIfMissing(url);

            // Expand short urls
            url = shortUrlResolver.resolveUrl(url);

            // Strip obvious pre request artifacts from the url to help with duplicate detection
            url = UrlFilters.stripFeedburnerParams(url);
            url = UrlFilters.stripPhpSession(url);

            log.debug("Cleaned url is: " + url);
            return url;

        } else {
            log.warn("Called with an empty url");
            return url;
        }
    }

}
