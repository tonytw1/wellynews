package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.OsmId;

public interface GeoCodeService {

	public List<Geocode> resolveAddress(String address);	
	public Geocode resolveOsmId(OsmId osmId);
	
}