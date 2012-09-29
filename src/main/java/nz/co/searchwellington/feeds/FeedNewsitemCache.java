package nz.co.searchwellington.feeds;

import java.util.List;

import net.spy.memcached.OperationTimeoutException;
import nz.co.searchwellington.caching.MemcachedCache;
import nz.co.searchwellington.model.FeedNewsitem;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeedNewsitemCache {
	
	private final Logger log = Logger.getLogger(FeedNewsitemCache.class);

	private static final String RSS_FEEDS_CACHE_PREFIX = "rssfeednewsitems:";
	private static final int ONE_DAY = 24 * 3600;
	
	private MemcachedCache cache;

	@Autowired
	public FeedNewsitemCache(MemcachedCache cache) {		
		this.cache = cache;
	}
	
	@SuppressWarnings("unchecked")
	public List<FeedNewsitem> getFeeditems(String url) {
		final String cacheKey = getCacheKey(url);
		try {
			List<FeedNewsitem> cachedResult = (List<FeedNewsitem>) cache.get(cacheKey);
			if (cachedResult != null) {			
				return cachedResult;
			}
		} catch (OperationTimeoutException e) {
			log.warn("Memcached connection timed out returning null result");
		}
		return null;
	}

	public void putFeedNewsitems(String url, List<FeedNewsitem> liveItems) {		
		if (liveItems != null) {
			final String cacheKey = getCacheKey(url);
			log.info("Caching " + liveItems.size() + " feed news items at: " + cacheKey);
			cache.put(cacheKey, ONE_DAY, liveItems);
		}
	}

	public void decache(String feedUrl) {
		log.debug("Decaching: " + feedUrl);
		cache.decache(feedUrl);		
	}
	
	private String getCacheKey(String url) {
		return RSS_FEEDS_CACHE_PREFIX + url;
	}
	
}
