package nz.co.searchwellington.urls;

import nz.co.searchwellington.urls.shorturls.CachingShortUrlResolverService;
import nz.co.searchwellington.utils.UrlFilters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Component
public class UrlCleaner {

    private final Logger log = Logger.getLogger(UrlCleaner.class);

    private final CachingShortUrlResolverService shortUrlResolver;

    @Autowired
    public UrlCleaner(CachingShortUrlResolverService shortUrlResolver) {
        this.shortUrlResolver = shortUrlResolver;
    }

    // Given a user or feed supplied parsed URL attempt to reduce it to a more canonical form
    public URL cleanSubmittedItemUrl(URL url) {
        try {
            // Resolve short urls
            URL expanded = shortUrlResolver.resolveUrl(url);

            // Strip obvious per request artifacts from the url to help with duplicate detection
            expanded = UrlFilters.stripUTMParams(expanded);
            expanded = UrlFilters.stripPhpSession(expanded);

            log.debug("Cleaned url is: " + expanded.toExternalForm());
            return expanded;

        } catch (URISyntaxException | MalformedURLException e) {
            log.warn("Invalid URL given; returning unaltered: " + url.toExternalForm());
            return url;
        }
    }

}
