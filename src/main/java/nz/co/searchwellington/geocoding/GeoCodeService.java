package nz.co.searchwellington.geocoding;

import nz.co.searchwellington.model.Geocode;

public interface GeoCodeService {

	public Geocode resolveAddress(String address);
	
}