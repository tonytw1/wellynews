package nz.co.searchwellington.geocoding;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Geocode;

import org.apache.log4j.Logger;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;

public class GoogleGeoCodeService implements GeoCodeService {

	private static Logger log = Logger.getLogger(GoogleGeoCodeService.class);

	private static final String REGION_RESTRICTION = "nz";	// TODO This probably wants to be an instance config option give we have uk copies running.
	
    public List<Geocode> resolveAddress(String address) {
    	GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(address).setRegion(REGION_RESTRICTION).setLanguage("en").getGeocoderRequest();		
		GeocodeResponse geocoderResponse = callResolver(address, geocoderRequest);
		
		if (geocoderResponse != null && geocoderResponse.getStatus().equals(GeocoderStatus.OK)) {
			List<Geocode> results = new ArrayList<Geocode>();
			for (GeocoderResult firstMatch : geocoderResponse.getResults()) {
				log.info("Address '" + address + "' resolved to: " + firstMatch);
				Geocode geocode = new Geocode();
				geocode.setAddress(address);
				geocode.setLatitude(firstMatch.getGeometry().getLocation().getLat().doubleValue());	// TODO mutating method
				geocode.setLongitude(firstMatch.getGeometry().getLocation().getLng().doubleValue());
						
				BigDecimal northEastBoundLat = firstMatch.getGeometry().getViewport().getNortheast().getLat();
				BigDecimal northEastBoundLng = firstMatch.getGeometry().getViewport().getNortheast().getLng();
				Geocode northEastBound = new Geocode(northEastBoundLat.doubleValue(), northEastBoundLng.doubleValue());
			
				BigDecimal southWestBoundLat = firstMatch.getGeometry().getViewport().getSouthwest().getLat();
				BigDecimal southWestBoundLng = firstMatch.getGeometry().getViewport().getSouthwest().getLng();
				
				final String type = firstMatch.getTypes().get(0);
				geocode.setType(type);
				final double boundingBoxSpan = northEastBound.getDistanceTo(southWestBoundLat.doubleValue(), southWestBoundLng.doubleValue());
				log.info("Type is: " + type + ", bounding box span is: " + boundingBoxSpan);
				
				results.add(geocode);
			}
			return results;
		}
		
		log.info("Resolver return non OK status: " + geocoderResponse.getStatus());
		return null;  
    }
    
	private GeocodeResponse callResolver(String address, GeocoderRequest geocoderRequest) {
		final Geocoder geocoder = new Geocoder();
		return geocoder.geocode(geocoderRequest);
	}
	
}
