package nz.co.searchwellington.urls.shorturls;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.common.caching.MemcachedCache;
import uk.co.eelpieconsulting.common.shorturls.ShortUrlResolver;
import uk.co.eelpieconsulting.common.shorturls.resolvers.*;

import java.net.URL;

@Component
public class CachingShortUrlResolverService {

    private static Logger log = Logger.getLogger(CachingShortUrlResolverService.class);

    private static final int ONE_DAY = 3600 * 24;
    private final static String KEY_PREFIX = "resolved-urls::";

    private ShortUrlResolver shortUrlResolverService;
    private MemcachedCache cache;

    @Autowired
    public CachingShortUrlResolverService(MemcachedCache cache) {
        shortUrlResolverService = new CompositeUrlResolver(new BitlyUrlResolver(), new FeedBurnerRedirectResolver(), new TinyUrlResolver(), new TwitterShortenerUrlResolver());
        this.cache = cache;
    }

    public String resolveUrl(String url) throws Exception {
        if (url != null && !url.isEmpty()) {
            try {
                URL parsed = new URL(url);
                if (!shortUrlResolverService.isValid(parsed)) {
                    return parsed.toExternalForm();
                }

                final String cachedResult = (String) cache.get(generateKey(parsed.toExternalForm()));
                if (cachedResult != null) {
                    log.debug("Found result for url '" + parsed.toExternalForm() + "' in cache: " + cachedResult);
                    return cachedResult;
                }

                log.debug("Delegating to live url resolver");
                final URL result = shortUrlResolverService.resolveUrl(parsed);
                if (result != null) {
                    putUrlIntoCache(url, result.toExternalForm());
                    return result.toExternalForm();
                }

                log.warn("Short url resolver return null for: " + parsed.toExternalForm());
                return parsed.toExternalForm();

            } catch (Exception e) {
                log.error("Failed to resolve short url for '" + url + "': ", e);
                throw e;
            }

        } else {
            log.warn("Called with empty url");
        }
        return url;
    }

    private void putUrlIntoCache(String url, String result) {
        log.debug("Caching result for url: " + url);
        cache.put(generateKey(url), ONE_DAY, result);
    }

    private String generateKey(String url) {
        return KEY_PREFIX + DigestUtils.sha256Hex(url);
    }

}
