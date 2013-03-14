package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import nz.co.searchwellington.model.OsmId;
import uk.co.eelpieconsulting.common.geo.Place;

public interface GeoCodeService {

	public List<Place> resolveAddress(String address);	
	public Place resolveOsmId(OsmId osmId);
	
}