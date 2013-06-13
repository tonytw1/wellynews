package nz.co.searchwellington.geocoding.osm;

import nz.co.searchwellington.model.OsmId;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.caching.CachableService;
import uk.co.eelpieconsulting.common.geo.NominatimGeocodingService;
import uk.co.eelpieconsulting.common.geo.model.Place;

@Component
public class NominatimResolveOsmIdCachingServiceWrapper implements CachableService<OsmId, Place> {

	private static final String OSM_ID_CACHE_PREFIX = "osmidgeocode:";
	private static final int ONE_DAY = 60 * 60 * 24;
	
	private NominatimGeocodingService nominatimGeocodingService;
	
	public NominatimResolveOsmIdCachingServiceWrapper() {
		this.nominatimGeocodingService = new NominatimGeocodingService("tony@eelpieconsulting.co.uk", "http://nominatim.openstreetmap.org/");
	}

	@Override
	public Place callService(OsmId osmId) {
		return nominatimGeocodingService.loadPlaceByOsmId(new uk.co.eelpieconsulting.common.geo.model.OsmId(osmId.getId(), osmId.getType()));
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
