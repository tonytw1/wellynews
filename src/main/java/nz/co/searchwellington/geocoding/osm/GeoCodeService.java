package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.Place;

public interface GeoCodeService {

	public List<Place> resolveAddress(String address);	
	public Place resolveOsmId(OsmId osmId);
	
}