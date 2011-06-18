package nz.co.searchwellington.geocoding;

import org.apache.log4j.Logger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import nz.co.searchwellington.model.Geocode;

public class CachingGeocodeService implements GeoCodeService {
	
	private static Logger log = Logger.getLogger(CachingGeocodeService.class);
	
	private static final String GEOCODE_CACHE_NAME = "geocodes";
	
	private GeoCodeService geoCodeService;	
	private Cache cache;
	
	public CachingGeocodeService(GeoCodeService geoCodeService, CacheManager manager) {
		this.geoCodeService = geoCodeService;
		this.cache = manager.getCache(GEOCODE_CACHE_NAME);
	}

	public Geocode resolveAddress(String address) {
		final String cacheKey = address;
		
		log.info("Resolving location for: " + address);
		if (cache != null) {
			Element cacheElement = cache.get(cacheKey);
			if (cacheElement != null) {
				log.info("Cache hit for: " + cacheKey);
				return (Geocode) cacheElement.getObjectValue();
			}
		}
		
		log.info("Cache miss for '" + cacheKey + "' - delegating to real resolver");
		Geocode resolvedGeocode = geoCodeService.resolveAddress(address);
		if (resolvedGeocode != null) {
			log.info("Caching resolved address for '" + cacheKey + "'");
			Element cacheElement = new Element(cacheKey, resolvedGeocode);
			cache.put(cacheElement);
		}
		return resolvedGeocode;
	}
	
}
