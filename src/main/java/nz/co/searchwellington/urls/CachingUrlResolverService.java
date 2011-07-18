package nz.co.searchwellington.urls;

import nz.co.searchwellington.repositories.keystore.KeyStore;

import org.apache.log4j.Logger;

// TODO This should cache with a long TTL, not forever. Good candiate for first memcached migration
public class CachingUrlResolverService extends UrlResolverService {
	
	private static Logger log = Logger.getLogger(CachingUrlResolverService.class);
	
	private KeyStore keystore;
	private String keyPrefix;
	
	public CachingUrlResolverService(KeyStore keystore, RedirectingUrlResolver... redirectResolvers) {
		super(redirectResolvers);
		this.keystore = keystore;
	}
	
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}
		
	@Override
	protected String resolveSingleUrl(String url) {		
		if (url != null && !url.isEmpty()) {
			
			final String cachedResult = keystore.get(generateKey(url));
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
		keystore.put(generateKey(url), result);
	}
	
	// TODO duplication with snapshotDAO
	private String generateKey(String id) {
		return keyPrefix + id;
	}
	
}
