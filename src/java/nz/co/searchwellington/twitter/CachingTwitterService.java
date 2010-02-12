package nz.co.searchwellington.twitter;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import nz.co.searchwellington.model.Twit;

import org.apache.log4j.Logger;

public class CachingTwitterService implements TwitterService {
	
	Logger log = Logger.getLogger(CachingTwitterService.class);
    
	private static final String TWITTER_REPLIES_CACHE = "twitterreplies";
	private static final String CACHE_KEY = "replies";
	
	private static final String TWEETS_CACHE = "tweets";
		


	private TwitterService twitterService;
	private CacheManager manager;
	
		
	public CachingTwitterService(TwitterService twitterService, CacheManager manager) {
		this.twitterService = twitterService;
		this.manager = manager;
	}
	
	

	@Override
	public String getUsername() {
		return twitterService.getUsername();
	}

	
	@Override
	public boolean isConfigured() {
		return twitterService.isConfigured();
	}

	
	@Override
	public Twit getTwitById(long twitterId) {
		Cache cache = manager.getCache(TWEETS_CACHE);		
		if (cache != null) {
			Element cacheElement = cache.get(CACHE_KEY);
			if (cacheElement != null && cacheElement.getObjectValue() != null) {
				Twit cachedResult = (Twit) cacheElement.getObjectValue();
				log.info("Found tweet in cache");
				return cachedResult;
			}
		}
		
		
		log.info("Delegrating to live twitter service");
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
