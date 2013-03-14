package nz.co.searchwellington.views;

import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.frontend.FrontendResource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.DistanceMeasuringService;
import uk.co.eelpieconsulting.common.geo.LatLong;

import com.google.common.collect.Lists;

@Component
public class GoogleMapsDisplayCleaner {
        
    private static Logger log = Logger.getLogger(GoogleMapsDisplayCleaner.class);
    
    private final DistanceMeasuringService distanceMeasuringService;
    
    @Autowired
    public GoogleMapsDisplayCleaner() {
		this.distanceMeasuringService = new DistanceMeasuringService();
	}
    
	public List<FrontendResource> dedupe(List<FrontendResource> geocoded) {
        return dedupe(geocoded, null);
    }

    public List<FrontendResource> dedupe(List<FrontendResource> geocoded, Resource selected) {      
        log.debug("Deduping collection with " + geocoded.size() + " items");
        final List<FrontendResource> deduped = Lists.newArrayList();
        if (selected != null && selected.getGeocode() != null) {
            deduped.add(selected);
        }
        
        for (FrontendResource resource : geocoded) {
        	if (resource.getGeocode() != null) {
        		boolean isUnique = !listAlreadyContainsResourceWithThisLocation(deduped, resource);
        		if (isUnique) {
        			deduped.add(resource);
        		}
        	}
        }
        
        log.info("Returning collection with " + deduped.size() + " items");
        return deduped;
    }
    
    private boolean listAlreadyContainsResourceWithThisLocation(List<FrontendResource> deduped, FrontendResource candidiate) {
        for (FrontendResource resource : deduped) {
            if (areSameOrOverlappingLocations(resource.getGeocode(), candidiate.getGeocode())) {
                log.debug("Rejected " + candidiate.getName() + " as it overlaps more recent item: " + resource.getGeocode().getAddress() + " / " + candidiate.getGeocode().getAddress());
                return true;
            }
        }
        return false;
    }

	private boolean areSameOrOverlappingLocations(Geocode geocode, Geocode geocode2) {
		final double distanceBetween = distanceMeasuringService.getDistanceBetween(new LatLong(geocode.getLatitude(), geocode.getLongitude()), 
				new LatLong(geocode2.getLatitude(), geocode2.getLongitude()));
		return distanceBetween < 0.1;
	}
    
}
