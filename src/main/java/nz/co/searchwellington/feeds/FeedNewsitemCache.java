package nz.co.searchwellington.feeds;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import nz.co.searchwellington.model.FeedNewsitem;

import org.apache.log4j.Logger;

public class FeedNewsitemCache {
	
	private static final String RSS_CACHE_NAME = "feeds";
	private final Logger log = Logger.getLogger(FeedNewsitemCache.class);
	
	private CacheManager manager;

	public FeedNewsitemCache(CacheManager manager) {		
		this.manager = manager;
	}

	@SuppressWarnings("unchecked")
	public List<FeedNewsitem> getFeeditems(String url) {
		Cache cache = manager.getCache(RSS_CACHE_NAME);

		if (cache != null) {
			Element cacheElement = cache.get(url);
			if (cacheElement != null) {
				List<FeedNewsitem> items = (List<FeedNewsitem>) cacheElement.getObjectValue();
				return items;
			}
		}
		return null;
	}

	public void putFeedNewsitems(String url, List<FeedNewsitem> liveItems) {
		Cache cache = manager.getCache(RSS_CACHE_NAME);
		log.debug("Caching feed items for url: " + url);
		if (cache != null && liveItems != null) {
			Element cachedFeedElement = new Element(url, liveItems);
			cache.put(cachedFeedElement);
		}
	}

	public void decache(String feedUrl) {
		Cache cache = manager.getCache(RSS_CACHE_NAME);
		log.debug("Decaching: " + feedUrl);
		cache.remove(feedUrl);		
	}
	
}
