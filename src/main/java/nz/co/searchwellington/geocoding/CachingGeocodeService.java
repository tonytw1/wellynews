package nz.co.searchwellington.geocoding;

import java.util.List;

import nz.co.searchwellington.caching.MemcachedCache;
import nz.co.searchwellington.model.Geocode;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CachingGeocodeService implements GeoCodeService {
	
	private static Logger log = Logger.getLogger(CachingGeocodeService.class);
	
	private static final String GEOCODE_CACHE_PREFIX = "geocodes:";
	private static final int ONE_DAY = 3600 * 24;
	
	private NominatimGeocodingService geoCodeService;	
	private MemcachedCache cache;
	
	@Autowired
	public CachingGeocodeService(NominatimGeocodingService geoCodeService, MemcachedCache cache) {
		this.geoCodeService = geoCodeService;
		this.cache = cache;
	}
	
	public List<Geocode> resolveAddress(String address) {
		final String cacheKey = GEOCODE_CACHE_PREFIX + address.replaceAll("\\s", "");
		log.info("Resolving location for: " + address);
		List<Geocode> cachedResult = (List<Geocode>) cache.get(cacheKey);
		if (cachedResult != null) {
			log.info("Cache hit for: " + cacheKey);
			return cachedResult;			
		}
		
		log.info("Cache miss for '" + cacheKey + "' - delegating to real resolver");
		List<Geocode> resolvedGeocode = geoCodeService.resolveAddress(address);
		if (resolvedGeocode != null) {
			log.info("Caching resolved address for '" + cacheKey + "'");
			cache.put(cacheKey, ONE_DAY, resolvedGeocode);
		}
		return resolvedGeocode;
	}
	
}
