package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.caching.CachableService;
import uk.co.eelpieconsulting.common.geo.NominatimGeocodingService;
import uk.co.eelpieconsulting.common.geo.Place;

@Component
public class NominatimResolveAddressCachingServiceWrapper implements CachableService<String, List<Place>> {

	private static final String OSM_ID_CACHE_PREFIX = "osmaddressgeocode:";
	private static final int ONE_DAY = 60 * 60 * 24;
	
	private NominatimGeocodingService nominatimGeocodingService;
	
	@Autowired
	public NominatimResolveAddressCachingServiceWrapper(NominatimGeocodingService nominatimGeocodingService) {
		this.nominatimGeocodingService = nominatimGeocodingService;
	}

	@Override
	public List<Place> callService(String placeName) {
		return nominatimGeocodingService.resolvePlaceName(placeName);
	}

	@Override
	public String getCacheKeyFor(String placeName) {
		return OSM_ID_CACHE_PREFIX + DigestUtils.md5Hex(placeName);
	}
	
	@Override
	public int getTTL() {
		return ONE_DAY;
	}
	
}
