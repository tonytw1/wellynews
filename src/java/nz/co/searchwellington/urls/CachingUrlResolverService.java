package nz.co.searchwellington.urls;

import org.apache.log4j.Logger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class CachingUrlResolverService extends UrlResolverService {
	
	Logger log = Logger.getLogger(CachingUrlResolverService.class);
	
	private static final String RESOLVED_URL_CACHE = "resolvedurls";

	private CacheManager manager;
	
	
	public CachingUrlResolverService(CacheManager manager,
		RedirectingUrlResolver... redirectResolvers) {
		super(redirectResolvers);
		this.manager = manager;
	}

	
	@Override
	protected String resolveSingleUrl(String url) {		
		if (url != null && !url.isEmpty()) {
			Cache cache = manager.getCache(RESOLVED_URL_CACHE);		
			if (cache != null) {
				Element cacheElement = cache.get(url);
				if (cacheElement != null && cacheElement.getObjectValue() != null) {
					String cachedResult = (String) cacheElement.getObjectValue();
					log.info("Found content for url '" + url + "' in cache: " + cachedResult);
					return cachedResult;
				}
			}
		
			log.info("Delegrating to live url resolver");
			final String fetchedResult = super.resolveSingleUrl(url);
			if (fetchedResult != null) {
				putUrlIntoCache(cache, url, fetchedResult);
			}
			return fetchedResult;
		} else {
			log.warn("Called with empty url");
		}
		return url;
	}

		
	private void putUrlIntoCache(Cache cache, String url, String result) {	
		log.info("Caching result for url: " + url);
		Element cachedResult = new Element(url, result);
		cache.put(cachedResult);		
	}
		
}
