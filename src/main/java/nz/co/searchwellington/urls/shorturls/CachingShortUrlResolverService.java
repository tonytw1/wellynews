package nz.co.searchwellington.urls.shorturls;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.caching.MemcachedCache;
import uk.co.eelpieconsulting.common.shorturls.BitlyUrlResolver;
import uk.co.eelpieconsulting.common.shorturls.FeedBurnerRedirectResolver;
import uk.co.eelpieconsulting.common.shorturls.ShortUrlResolverService;
import uk.co.eelpieconsulting.common.shorturls.TinyUrlResolver;
import uk.co.eelpieconsulting.common.shorturls.TwitterShortenerUrlResolver;

@Component
public class CachingShortUrlResolverService {

    private static Logger log = Logger.getLogger(CachingShortUrlResolverService.class);

    private static final int ONE_DAY = 3600 * 24;
    private final static String KEY_PREFIX = "resolved-urls::";

    private ShortUrlResolverService shortUrlResolverService;
    private MemcachedCache cache;

    @Autowired
    public CachingShortUrlResolverService(MemcachedCache cache) {
        shortUrlResolverService = new ShortUrlResolverService(new BitlyUrlResolver(), new FeedBurnerRedirectResolver(), new TinyUrlResolver(), new TwitterShortenerUrlResolver());
        this.cache = cache;
    }

    public String resolveUrl(String url) {
        if (url != null && !url.isEmpty()) {
            final String cachedResult = (String) cache.get(generateKey(url));
            if (cachedResult != null) {
                log.debug("Found result for url '" + url + "' in cache: " + cachedResult);
                return cachedResult;
            }

            log.debug("Delegating to live url resolver");
            final String result = shortUrlResolverService.resolveUrl(url);
            if (result != null) {
                putUrlIntoCache(url, result);
            }
            return result;

        } else {
            log.warn("Called with empty url");
        }
        return url;
    }

    private void putUrlIntoCache(String url, String result) {
        log.debug("Caching result for url: " + url);
        cache.put(generateKey(url), ONE_DAY, result);
    }

    private String generateKey(String id) {
        return KEY_PREFIX + id;
    }

}