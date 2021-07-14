package nz.co.searchwellington.geocoding.osm;

import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.Place;

public interface GeoCodeService {

	public Place resolveOsmId(OsmId osmId);
	
}