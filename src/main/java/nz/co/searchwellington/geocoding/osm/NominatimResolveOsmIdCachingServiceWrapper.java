package nz.co.searchwellington.geocoding.osm;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.caching.CachableService;
import uk.co.eelpieconsulting.common.geo.NominatimGeocodingService;
import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.Place;

@Component
public class NominatimResolveOsmIdCachingServiceWrapper implements CachableService<OsmId, Place> {

	private static final String OSM_ID_CACHE_PREFIX = "osmidgeocode:";
	private static final int ONE_DAY = 60 * 60 * 24;
	
	private NominatimGeocodingService nominatimGeocodingService;
    
    @Autowired
	public NominatimResolveOsmIdCachingServiceWrapper(@Value("#{config['nominatim.url']}") String nominatimUrl) {
		this.nominatimGeocodingService = new NominatimGeocodingService("tony@eelpieconsulting.co.uk", nominatimUrl);
	}

	@Override
	public Place callService(OsmId osmId) {
		return nominatimGeocodingService.loadPlaceByOsmId(osmId);
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
