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

    Logger log = Logger.getLogger(UrlCleaner.class);

    private final CachingShortUrlResolverService shortUrlResolver;

    @Autowired
    public UrlCleaner(CachingShortUrlResolverService shortUrlResolver) {
        this.shortUrlResolver = shortUrlResolver;
    }

    public String cleanSubmittedItemUrl(String urlString) {
        if (!urlString.isEmpty()) {
            try {
                // Trim and add prefix is missing from user submitted input
                urlString = UrlFilters.trimWhiteSpace(urlString);
                urlString = UrlFilters.addHttpPrefixIfMissing(urlString);

                // Expand short urls
                URL url = new URL(urlString);  // TODO nudge this step up
                URL expanded = shortUrlResolver.resolveUrl(url);

                // Strip obvious pre request artifacts from the url to help with duplicate detection
                expanded = UrlFilters.stripUTMParams(expanded);
                expanded = UrlFilters.stripPhpSession(expanded);

                log.debug("Cleaned url is: " + expanded.toExternalForm());
                return expanded.toExternalForm();

            } catch (URISyntaxException | MalformedURLException e) {
                log.warn("Invalid URL given; returning unaltered: " + urlString);
                return urlString;
            }

        } else {
            log.warn("Called with an empty url");
            return urlString;
        }
    }

}
