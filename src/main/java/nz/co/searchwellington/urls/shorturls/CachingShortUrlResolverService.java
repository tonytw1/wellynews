package nz.co.searchwellington.urls.shorturls;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.common.caching.MemcachedCache;
import uk.co.eelpieconsulting.common.shorturls.ShortUrlResolver;
import uk.co.eelpieconsulting.common.shorturls.resolvers.*;

import java.net.URI;
import java.net.URL;

@Component
public class CachingShortUrlResolverService {

    private static final Logger log = Logger.getLogger(CachingShortUrlResolverService.class);

    private static final int ONE_DAY = 3600 * 24;
    private final static String KEY_PREFIX = "resolved-shorturls::";

    private ShortUrlResolver shortUrlResolverService;
    private MemcachedCache cache;

    @Autowired
    public CachingShortUrlResolverService(MemcachedCache cache) {
        shortUrlResolverService = new CompositeUrlResolver(new BitlyUrlResolver(), new FeedBurnerRedirectResolver(), new TinyUrlResolver(), new TwitterShortenerUrlResolver());
        this.cache = cache;
    }

    public URI resolveUrl(URI uri) throws Exception {
        try {
            URL parsed = uri.toURL();
            if (!shortUrlResolverService.isValid(parsed)) {
                return uri;
            }

            final URI cachedResult = (URI) cache.get(generateKey(parsed));
            if (cachedResult != null) {
                log.debug("Found result for url '" + parsed.toExternalForm() + "' in cache: " + cachedResult);
                return cachedResult;
            }

            log.debug("Delegating to live url resolver");
            final URL result = shortUrlResolverService.resolveUrl(parsed);
            if (result != null) {
                putUrlIntoCache(result, result.toExternalForm());
                return result.toURI();
            }

            log.warn("Short url resolver returned null for: " + parsed.toExternalForm());
            return uri;

        } catch (Exception e) {
            log.error("Failed to resolve short url for '" + uri.toURL().toExternalForm() + "': ", e);
            throw e;
        }
    }

    private void putUrlIntoCache(URL url, String result) {
        log.debug("Caching result for url: " + url.toExternalForm());
        cache.put(generateKey(url), ONE_DAY, result);
    }

    private String generateKey(URL url) {
        return KEY_PREFIX + DigestUtils.sha256Hex(url.toExternalForm());
    }

}
