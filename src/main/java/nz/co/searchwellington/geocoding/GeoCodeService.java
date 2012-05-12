package nz.co.searchwellington.geocoding;

import java.util.List;

import nz.co.searchwellington.model.Geocode;

public interface GeoCodeService {

	public List<Geocode> resolveAddress(String address);
	
}