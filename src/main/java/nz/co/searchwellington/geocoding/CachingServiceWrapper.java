package nz.co.searchwellington.geocoding;

import nz.co.searchwellington.caching.MemcachedCache;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CachingServiceWrapper <T, U> {

	private static Logger log = Logger.getLogger(CachingServiceWrapper.class);
	
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
		
		log.info("Cache miss for '" + cacheKey + "' - delegating to real service");
		final U result = service.callService(parameter);
		if (result != null) {
			log.info("Caching result for :" + cacheKey);
			cache.put(cacheKey, service.getTTL(), result);
			return result;
		}
		
		log.info("Live service call failed; returning null");
		return null;
	}
	
}
