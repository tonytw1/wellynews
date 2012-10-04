package nz.co.searchwellington.geocoding;

import java.util.Collections;
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
	private static final String OSM_ID_CACHE_PREFIX = "osmidgeocode:";
	private static final String NOT_RESOLVED = "Not resolved";
	
	private static final int TWO_DAYS = 3600 * 48;
	
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
			if (isNegativeHit(cachedResult)) {
				log.info("Negative cache hit for: " + cacheKey);
				return null;
			}
			log.info("Cache hit for: " + cacheKey);
			return cachedResult;			
		}
		
		log.info("Cache miss for '" + cacheKey + "' - delegating to real resolver");
		List<Geocode> resolvedGeocode = geoCodeService.resolveAddress(address);
		if (resolvedGeocode != null) {
			log.info("Caching resolved address for '" + cacheKey + "'");
			cache.put(cacheKey, TWO_DAYS, resolvedGeocode);
			return resolvedGeocode;
		}
		
		log.info("Failed to resolve; marking negative cache hit for: " + cacheKey);
		cache.put(cacheKey, TWO_DAYS, Collections.emptyList());
		return null;
	}
	
	public Geocode resolveAddress(String osmType, long osmId) {
		final String cacheKey = OSM_ID_CACHE_PREFIX + osmId + osmType;
		log.info("Resolving location for: " + osmType + osmId);
		
		Geocode cachedResult = (Geocode) cache.get(cacheKey);
		if (cachedResult != null) {
			if (isNegativeHit(cachedResult)) {
				log.info("Negative cache hit for: " + cacheKey);
				return null;
			}
			log.info("Cache hit for: " + cacheKey);
			return cachedResult;			
		}
		
		log.info("Cache miss for '" + cacheKey + "' - delegating to real resolver");
		final Geocode resolvedGeocode = geoCodeService.resolveAddress(osmType, osmId);
		if (resolvedGeocode != null) {
			log.info("Caching resolved address for '" + cacheKey + "'");
			cache.put(cacheKey, TWO_DAYS, resolvedGeocode);
			return resolvedGeocode;
		}
		
		log.info("Failed to resolve; marking negative cache hit for: " + cacheKey);
		cache.put(cacheKey, TWO_DAYS, new Geocode(NOT_RESOLVED, null, null));
		return null;
	}

	private boolean isNegativeHit(Geocode cachedResult) {
		return cachedResult.getAddress().equals(NOT_RESOLVED);
	}

	private boolean isNegativeHit(List<Geocode> cachedResult) {
		return cachedResult.isEmpty();
	}
	
}
