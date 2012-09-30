package nz.co.searchwellington.urls;

import nz.co.searchwellington.caching.MemcachedCache;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CachingUrlResolverService extends UrlResolverService {
	
	private static Logger log = Logger.getLogger(CachingUrlResolverService.class);

	private static final int ONE_DAY = 3600 * 24;
	
	private MemcachedCache cache;
	
	private final String keyPrefix = "resolved-urls::";
	
	@Autowired
	public CachingUrlResolverService(MemcachedCache cache, RedirectingUrlResolver... redirectResolvers) {
		super(redirectResolvers);
		this.cache = cache;
	}
		
	@Override
	protected String resolveSingleUrl(String url) {		
		if (url != null && !url.isEmpty()) {			
			final String cachedResult = (String) cache.get(generateKey(url));
			if (cachedResult != null) {
				log.info("Found content for url '" + url + "' in cache: " + cachedResult);
				return cachedResult;				
			}
		
			log.info("Delegrating to live url resolver");
			final String fetchedResult = super.resolveSingleUrl(url);
			if (fetchedResult != null) {
				putUrlIntoCache(url, fetchedResult);
			}
			return fetchedResult;
			
		} else {
			log.warn("Called with empty url");
		}
		return url;
	}

	private void putUrlIntoCache(String url, String result) {	
		log.info("Caching result for url: " + url);
		cache.put(generateKey(url), ONE_DAY, result);
	}
	
	// TODO duplication with snapshotDAO
	private String generateKey(String id) {
		return keyPrefix + id;
	}
	
}
