package nz.co.searchwellington.twitter;

import java.util.List;

import nz.co.searchwellington.caching.MemcachedCache;
import nz.co.searchwellington.model.Twit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CachingTwitterService implements TwitterService {
	
	private static Logger log = Logger.getLogger(CachingTwitterService.class);
    
	private static final int ONE_HOUR = 3600;
	private static final int ONE_DAY = 24 * ONE_HOUR;
	private static final int ONE_WEEK = 7 * ONE_DAY;
	
	private static final String TWITTER_PROFILE_IMAGE_CACHE_PREFIX = "twitterprofileimage";
	private static final String TWEETS_CACHE_PREFIX = "tweets:";
	private static final String TWITTER_REPLIES_CACHE = "twitterreplies:";
	
	private TwitterService twitterService;
	private MemcachedCache cache;

    @Value("#{config['twitter.username']}")
	private String twitterUsername;
	
    @Autowired
	public CachingTwitterService(TwitterService twitterService, MemcachedCache cache) {
		this.twitterService = twitterService;
		this.cache = cache;
	}
    
	@Override
	public Twit getTwitById(long twitterId) {
		Twit cachedTweet = (Twit) cache.get(TWEETS_CACHE_PREFIX + Long.toString(twitterId));
		if (cachedTweet != null) {
			log.debug("Found tweet in cache");
			return cachedTweet;		
		}
		
		log.debug("Delegrating to live twitter service");
		Twit tweet = twitterService.getTwitById(twitterId);
		cache.put(TWEETS_CACHE_PREFIX + tweet.getId(), ONE_WEEK, tweet);		
		return tweet;		
	}

	@SuppressWarnings("unchecked")
	public List<Twit> getReplies() {	
		final String cacheKey = TWITTER_REPLIES_CACHE + twitterUsername;
		List<Twit> cachedResults = (List<Twit>) cache.get(cacheKey);
		if (cachedResults != null) {
			log.debug("Found replies in cache");
			return cachedResults;
		}
		
		log.info("Delegrating to live twitter service");
		final List<Twit> fetchedResults = (List<Twit>) twitterService.getReplies();
		if (fetchedResults != null) {
			cache.put(cacheKey, ONE_HOUR, fetchedResults);
		}
		return fetchedResults;
	}
	
	@Override
	public String getTwitterProfileImageUrlFor(String twitterUsername) {
		final String cacheKey = TWITTER_PROFILE_IMAGE_CACHE_PREFIX + twitterUsername;
		
		final String cachedResult = (String) cache.get(cacheKey);
		log.info("cached: " + cachedResult);
		if (cachedResult != null && !cachedResult.isEmpty()) {
			return cachedResult;
		}
		if (cachedResult != null && cachedResult.isEmpty()) {
			log.debug("Returning negitive cache hit for twitter profile image: " + twitterUsername);
			return null;
		}
		
		final String twitterProfileImageUrlFor = twitterService.getTwitterProfileImageUrlFor(twitterUsername);
		if (twitterProfileImageUrlFor != null) {
			cache.put(cacheKey, ONE_DAY, twitterProfileImageUrlFor);
		} else {
			log.debug("Caching negitive result for twitter profile image: " + twitterUsername);
			cache.put(cacheKey, ONE_HOUR, "");
		}
		return twitterProfileImageUrlFor;
	}
	
	@Override
	public boolean isConfigured() {
		return twitterService.isConfigured();
	}
	
}
