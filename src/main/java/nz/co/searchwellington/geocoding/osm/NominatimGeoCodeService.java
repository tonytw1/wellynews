package nz.co.searchwellington.geocoding.osm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.common.geo.NominatimGeocodingService;
import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.Place;

@Component
public class NominatimGeoCodeService implements GeoCodeService {

	private NominatimGeocodingService nominatimGeocodingService;
    
    @Autowired
	public NominatimGeoCodeService(@Value("${nominatim.url}") String nominatimUrl) {
		this.nominatimGeocodingService = new NominatimGeocodingService("tony@eelpieconsulting.co.uk", nominatimUrl);
	}

	@Override
	public Place resolveOsmId(OsmId osmId) {
		return nominatimGeocodingService.loadPlaceByOsmId(osmId);
	}

}
