package nz.co.searchwellington.geocoding;

import nz.co.searchwellington.model.Geocode;

public class CompositeGeocodeService implements GeoCodeService {

	private GeoCodeService[] geocoders;
	
	public CompositeGeocodeService(GeoCodeService... geocoders) {
		this.geocoders = geocoders;
	}

	@Override
	public Geocode resolveAddress(String address) {
		for (int i = 0; i < geocoders.length; i++) {
			GeoCodeService geoCodeService = geocoders[i];
			Geocode geocode = geoCodeService.resolveAddress(address);
			if(geocode != null) {
				return geocode;
			}
		}
		return null;
	}
	
}
