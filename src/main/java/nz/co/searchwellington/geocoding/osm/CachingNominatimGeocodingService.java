package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.Place;

@Component
public class CachingNominatimGeocodingService implements GeoCodeService {
	
	private CachingNominatimResolveOsmIdService cachingNominatimResolveOsmIdService;
	
	@Autowired
	public CachingNominatimGeocodingService(CachingNominatimResolveOsmIdService cachingNominatimResolveOsmIdService) {
		this.cachingNominatimResolveOsmIdService = cachingNominatimResolveOsmIdService;
	}

	@Override
	public Place resolveOsmId(OsmId osmId) {
		return cachingNominatimResolveOsmIdService.callService(osmId);
	}
	
}
