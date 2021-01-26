package nz.co.searchwellington.geocoding.osm;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.common.caching.CachableService;
import uk.co.eelpieconsulting.common.geo.model.Place;

import java.util.List;

@Component
public class NominatimResolveAddressCachingServiceWrapper implements CachableService<String, List<Place>> {

	private static final String OSM_ID_CACHE_PREFIX = "osmaddressgeocode:";
	private static final int ONE_DAY = 60 * 60 * 24;
	
	//private NominatimGeocodingService nominatimGeocodingService;
	
	public NominatimResolveAddressCachingServiceWrapper() {
		//this.nominatimGeocodingService = new NominatimGeocodingService("tony@eelpieconsulting.co.uk", "http://nominatim.openstreetmap.org/");
	}

	@Override
	public List<Place> callService(String placeName) {
		return null; //nominatimGeocodingService.resolvePlaceName(placeName);
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
