package nz.co.searchwellington.geocoding;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderLocationType;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;

import nz.co.searchwellington.model.Geocode;

import org.apache.log4j.Logger;

public class GoogleGeoCodeService {

    private static Logger log = Logger.getLogger(GoogleGeoCodeService.class);
    
    public GoogleGeoCodeService() {      
    }
    
    public void resolveAddress(Geocode geocode) {
        if (geocode != null && geocode.getAddress() != null) {
        	resolveAddressOfGeocode(geocode);
        } else {
            log.warn("No geocode address to resolve.");
        }
    }
    
	private void resolveAddressOfGeocode(Geocode geocode) {
		final Geocoder geocoder = new Geocoder();
		final String addressString = geocode.getAddress();				
		GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(addressString).setRegion("nz").setLanguage("en").getGeocoderRequest();
		GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
		log.info("Address '" + addressString + "' resolved to: " + geocoderResponse);
		if (geocoderResponse.getStatus().equals(GeocoderStatus.OK)) {
			final GeocoderResult firstMatch = geocoderResponse.getResults().get(0);
			if (isSuitableMatchInCorrectRegion(firstMatch)) {
				geocode.setLatitude(firstMatch.getGeometry().getLocation().getLat().doubleValue());
				geocode.setLongitude(firstMatch.getGeometry().getLocation().getLng().doubleValue());
			}
		}
	}
	
	private boolean isSuitableMatchInCorrectRegion(final GeocoderResult firstMatch) {
		final GeocoderLocationType locationType = firstMatch.getGeometry().getLocationType();
		if (locationType.equals(GeocoderLocationType.RANGE_INTERPOLATED) || locationType.equals(GeocoderLocationType.ROOFTOP)) {
			for (GeocoderAddressComponent addressComponent : firstMatch.getAddressComponents()) {
				if (addressComponent.getTypes().contains("administrative_area_level_1") && addressComponent.getLongName().equals("Wellington")) {
					return true;
				}
			}
			return true;
		}
		return false;
	}
    
}
