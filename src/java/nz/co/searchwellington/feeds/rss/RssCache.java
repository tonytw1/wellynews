package nz.co.searchwellington.feeds.rss;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndFeed;

public class RssCache {

	private static final String RSS_CACHE_NAME = "feeds";
	private final Logger log = Logger.getLogger(RssCache.class);

	
	public SyndFeed getFeedByUrl(String url) {
		try {
			CacheManager manager = CacheManager.create();
			Cache cache = manager.getCache(RSS_CACHE_NAME);

			if (cache != null) {
				Element cacheElement = cache.get(url);
				if (cacheElement != null) {
					SyndFeed syndfeed = (SyndFeed) cacheElement.getObjectValue();
					log.debug("Found syndfeed for feed in cache: " + url);
					return syndfeed;
				}
			}

		} catch (CacheException e) {
			log.error("CacheException while trying to access syndfeed from cache.", e);
		}
		return null;
	}

	
	public void putFeedIntoCache(String url, SyndFeed syndfeed) {
		log.info("Caching syndfeed for url: " + url);
		try {
			CacheManager manager = CacheManager.create();
			Cache cache = manager.getCache(RSS_CACHE_NAME);

			if (cache != null && syndfeed != null) {
				Element cachedFeedElement = new Element(url, syndfeed);
				cache.put(cachedFeedElement);
			}
		} catch (CacheException e) {
			log.error("CacheException while trying to cache syndfeed.", e);
		}
	}

}
