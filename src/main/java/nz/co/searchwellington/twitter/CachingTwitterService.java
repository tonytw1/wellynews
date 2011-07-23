package nz.co.searchwellington.twitter;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import nz.co.searchwellington.caching.MemcachedCache;
import nz.co.searchwellington.model.Twit;

import org.apache.log4j.Logger;

public class CachingTwitterService implements TwitterService {
	
	private static Logger log = Logger.getLogger(CachingTwitterService.class);
    
	private static final int ONE_DAY = 24 * 3600;
	private static final String TWITTER_PROFILE_IMAGE_CACHE_PREFIX = "twitterprofileimage";
	private static final String TWITTER_REPLIES_CACHE = "twitterreplies";
	private static final String CACHE_KEY = "replies";	
	private static final String TWEETS_CACHE = "tweets";
	
	private TwitterService twitterService;
	@Deprecated // TODO migrate to memcached for application cache
	private CacheManager manager;
	private MemcachedCache cache;
	
	public CachingTwitterService(TwitterService twitterService, CacheManager manager, MemcachedCache cache) {
		this.twitterService = twitterService;
		this.manager = manager;
		this.cache = cache;
	}
	
	@Override
	public Twit getTwitById(long twitterId) {
		Cache cache = manager.getCache(TWEETS_CACHE);		
		if (cache != null) {
			Element cacheElement = cache.get(CACHE_KEY);
			if (cacheElement != null && cacheElement.getObjectValue() != null) {
				Twit cachedResult = (Twit) cacheElement.getObjectValue();
				log.debug("Found tweet in cache");
				return cachedResult;
			}
		}
		
		log.debug("Delegrating to live twitter service");
		Twit tweet = twitterService.getTwitById(twitterId);
		putTweetIntoCache(cache, tweet);		
		return tweet;		
	}

	@SuppressWarnings("unchecked")
	public List<Twit> getReplies() {
		Cache cache = manager.getCache(TWITTER_REPLIES_CACHE);		
		if (cache != null) {
			Element cacheElement = cache.get(CACHE_KEY);
			if (cacheElement != null && cacheElement.getObjectValue() != null) {
				List<Twit> cachedResult = (List<Twit>) cacheElement.getObjectValue();
				log.info("Found replies in cache");
				return cachedResult;
			}
		}
		
		log.info("Delegrating to live twitter service");
		final List<Twit> fetchedResults = (List<Twit>) twitterService.getReplies();
		if (fetchedResults != null) {
			putIntoCache(cache, fetchedResults);
		}
		return fetchedResults;
	}
	
	@Override
	public String getTwitterProfileImageUrlFor(String twitterUsername) {
		final String cachedResult = (String) cache.get(TWITTER_PROFILE_IMAGE_CACHE_PREFIX + twitterUsername);
		if (cachedResult != null) {
			return cachedResult;
		}
		final String twitterProfileImageUrlFor = twitterService.getTwitterProfileImageUrlFor(twitterUsername);
		if (twitterProfileImageUrlFor != null) {
			cache.put(TWITTER_PROFILE_IMAGE_CACHE_PREFIX + twitterUsername, ONE_DAY, twitterProfileImageUrlFor);
		}
		return twitterProfileImageUrlFor;
	}
	
	@Override
	public boolean isConfigured() {
		return twitterService.isConfigured();
	}
	
	private void putIntoCache(Cache cache, List<Twit> results) {	
		log.info("Caching result");
		Element cachedResult = new Element(CACHE_KEY, results);
		cache.put(cachedResult);
	}
	
	private void putTweetIntoCache(Cache cache, Twit tweet) {
		log.info("Caching result");
		Element cachedResult = new Element(tweet.getId(), tweet);
		cache.put(cachedResult);		
	}
	
}
