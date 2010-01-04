package nz.co.searchwellington.urls;

import nz.co.searchwellington.repositories.redis.KeyStore;

import org.apache.log4j.Logger;

public class CachingUrlResolverService extends UrlResolverService {
	
	Logger log = Logger.getLogger(CachingUrlResolverService.class);
	
	private KeyStore resolvedUrlsCache;
	
	
	public CachingUrlResolverService(KeyStore resolvedUrlsCache, RedirectingUrlResolver... redirectResolvers) {
		super(redirectResolvers);
		this.resolvedUrlsCache = resolvedUrlsCache;
	}

	
	@Override
	protected String resolveSingleUrl(String url) {		
		if (url != null && !url.isEmpty()) {
			
			final String cachedResult = resolvedUrlsCache.get(url);
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
		resolvedUrlsCache.put(url, result);
	}
	
}
