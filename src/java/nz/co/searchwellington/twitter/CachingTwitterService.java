package nz.co.searchwellington.twitter;

import org.apache.log4j.Logger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.unto.twitter.Status;

public class CachingTwitterService implements TwitterService {
	
	Logger log = Logger.getLogger(CachingTwitterService.class);
    
	private static final String TWITTER_REPLIES_CACHE = "twitterreplies";
	private static final String CACHE_KEY = "replies";

	private TwitterService twitterService;
	private CacheManager manager;
	
		
	public CachingTwitterService(TwitterService twitterService, CacheManager manager) {
		this.twitterService = twitterService;
		this.manager = manager;
	}

	
	@Override
	public boolean isConfigured() {
		return twitterService.isConfigured();
	}

	
	public Status[] getReplies() {
		Cache cache = manager.getCache(TWITTER_REPLIES_CACHE);		
		if (cache != null) {
			Element cacheElement = cache.get(CACHE_KEY);
			if (cacheElement != null && cacheElement.getObjectValue() != null) {
				Status[] cachedResult = (Status[]) cacheElement.getObjectValue();
				log.info("Found replies in cache");
				return cachedResult;
			}
		}
		
		log.info("Delegrating to live twitter service");
		final Status[] fetchedResults = twitterService.getReplies();
		if (fetchedResults != null) {
			putIntoCache(cache, fetchedResults);
		}
		return fetchedResults;
	}

	
	private void putIntoCache(Cache cache, Status[] results) {	
		log.info("Caching result");
		Element cachedResult = new Element(CACHE_KEY, results);
		cache.put(cachedResult);
	}
	
}
