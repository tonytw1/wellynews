package nz.co.searchwellington.geocoding;

import nz.co.searchwellington.caching.MemcachedCache;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CachingServiceWrapper <T, U> {

	private static Logger log = Logger.getLogger(CachingServiceWrapper.class);
	
	private static final String NEGATIVE = "negative";
	
	private CachableService<T, U> service;
	private MemcachedCache cache;
	
	@Autowired
	public CachingServiceWrapper(CachableService<T, U> service, MemcachedCache cache) {
		super();
		this.service = service;
		this.cache = cache;
	}

	public U callService(T parameter) {
		final String cacheKey = service.getCacheKeyFor(parameter);

		final U cachedResult = (U) cache.get(cacheKey);
		if (cachedResult != null) {
			log.info("Cache hit for: " + cacheKey);
			return cachedResult;
		}
		
		log.info("Cache miss for '" + cacheKey + "' - checking for negative cache hit");
		if (isNegativeCacheHit(cacheKey)) {
			log.info("Negative cache hit for '" + cacheKey + "'; returning null");
			return null;
		}
		
		log.info("Cache miss for '" + cacheKey + "' - delegating to real service");
		final U result = service.callService(parameter);
		if (result != null) {
			log.info("Caching result for :" + cacheKey);
			cache.put(cacheKey, service.getTTL(), result);
			return result;
		}
		
		log.info("Live service call failed; returning adding negative cache and returning null");
		setNegativeCacheHit(cacheKey);
		return null;
	}

	private void setNegativeCacheHit(String cacheKey) {
		cache.put(negativeCacheKeyFor(cacheKey) , service.getTTL(), 1);
	}
	
	private boolean isNegativeCacheHit(String cacheKey) {
		return cache.get(negativeCacheKeyFor(cacheKey)) != null;
	}
	
	private String negativeCacheKeyFor(String cacheKey) {
		return NEGATIVE + cacheKey;
	}
	
}
