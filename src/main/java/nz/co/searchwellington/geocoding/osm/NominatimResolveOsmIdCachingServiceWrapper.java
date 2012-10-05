package nz.co.searchwellington.geocoding.osm;

import nz.co.searchwellington.geocoding.CachableService;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.OsmId;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NominatimResolveOsmIdCachingServiceWrapper implements CachableService<OsmId, Geocode> {

	private static final String OSM_ID_CACHE_PREFIX = "osmidgeocode:";
	private static final int ONE_DAY = 60 * 60 * 24;
	
	private NominatimGeocodingService nominatimGeocodingService;
	
	@Autowired
	public NominatimResolveOsmIdCachingServiceWrapper(NominatimGeocodingService nominatimGeocodingService) {
		this.nominatimGeocodingService = nominatimGeocodingService;
	}

	@Override
	public Geocode callService(OsmId osmId) {
		return nominatimGeocodingService.resolveOsmId(osmId);
	}

	@Override
	public String getCacheKeyFor(OsmId osmId) {
		return OSM_ID_CACHE_PREFIX + DigestUtils.md5Hex(osmId.toString());
	}
	
	@Override
	public int getTTL() {
		return ONE_DAY;
	}
	
}
