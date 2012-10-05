package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import nz.co.searchwellington.geocoding.CachableService;
import nz.co.searchwellington.model.Geocode;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NominatimResolveAddressCachingServiceWrapper implements CachableService<String, List<Geocode>> {

	private static final String OSM_ID_CACHE_PREFIX = "osmaddressgeocode:";
	private static final int ONE_DAY = 60 * 60 * 24;
	
	private NominatimGeocodingService nominatimGeocodingService;
	
	@Autowired
	public NominatimResolveAddressCachingServiceWrapper(NominatimGeocodingService nominatimGeocodingService) {
		this.nominatimGeocodingService = nominatimGeocodingService;
	}

	@Override
	public List<Geocode> callService(String address) {
		return nominatimGeocodingService.resolveAddress(address);
	}

	@Override
	public String getCacheKeyFor(String address) {
		return OSM_ID_CACHE_PREFIX + DigestUtils.md5Hex(address);
	}
	
	@Override
	public int getTTL() {
		return ONE_DAY;
	}
	
}
