package nz.co.searchwellington.urls;

import nz.co.searchwellington.urls.shorturls.CachingShortUrlResolverService;
import nz.co.searchwellington.utils.UrlFilters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Component
public class UrlCleaner {

    Logger log = Logger.getLogger(UrlCleaner.class);

    private CachingShortUrlResolverService shortUrlResolver;

    @Autowired
    public UrlCleaner(CachingShortUrlResolverService shortUrlResolver) {
        this.shortUrlResolver = shortUrlResolver;
    }

    public String cleanSubmittedItemUrl(String url) throws Exception {
        if (!url.isEmpty()) {
            try {

                // Trim and add prefix is missing from user submitted input
                url = UrlFilters.trimWhiteSpace(url);
                url = UrlFilters.addHttpPrefixIfMissing(url);

                // Expand short urls
                url = shortUrlResolver.resolveUrl(url);

                // Strip obvious pre request artifacts from the url to help with duplicate detection
                URI uri = new URL(url).toURI(); // TODO nudge this step up
                uri = UrlFilters.stripUTMParams(uri);
                uri = UrlFilters.stripPhpSession(uri);

                log.debug("Cleaned url is: " + uri.toURL().toExternalForm());
                return uri.toURL().toExternalForm();

            } catch (URISyntaxException | MalformedURLException e) {
                log.warn("Invalid URL given; returning unaltered: " + url);
                return url;
            }

        } else {
            log.warn("Called with an empty url");
            return url;
        }
    }

}
