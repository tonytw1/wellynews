package nz.co.searchwellington.geo;

import geo.google.datamodel.GeoAltitude;
import geo.google.datamodel.GeoCoordinate;
import geo.google.datamodel.GeoUtils;
import nz.co.searchwellington.model.Geocode;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class DistanceMeasuringService {
	
	private static Logger log = Logger.getLogger(DistanceMeasuringService.class);
	
    public boolean areSameLocation(Geocode here, Geocode there) {        
        final double distanceBetweenInKm = getDistanceBetween(here.getLatitude(), here.getLongitude(), there.getLatitude(), there.getLongitude());
        log.debug("Points " + here.getAddress() + " and " + there.getAddress() + " are " + distanceBetweenInKm + " km part");
        return distanceBetweenInKm < 0.1;
    }
    
	private double getDistanceBetween(double thereLatitude, double thereLongitude, double hereLatitude, double hereLongitude) {
		final GeoCoordinate here = new GeoCoordinate(hereLatitude, hereLongitude, new GeoAltitude(0));
		final GeoCoordinate there = new GeoCoordinate(thereLatitude, thereLongitude, new GeoAltitude(0));
		final double distanceBetweenInKm = GeoUtils.distanceBetweenInKm(here, there);
		log.debug("Distance to " + thereLatitude + ", " + thereLongitude + " is " + distanceBetweenInKm);
		return distanceBetweenInKm;
	}
	
}
