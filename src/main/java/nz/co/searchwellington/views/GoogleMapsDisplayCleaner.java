package nz.co.searchwellington.views;

import java.util.List;

import nz.co.searchwellington.model.frontend.FrontendResource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.DistanceMeasuringService;
import uk.co.eelpieconsulting.common.geo.model.Place;

import com.google.common.collect.Lists;

@Component
public class GoogleMapsDisplayCleaner {
        
    private static final double ONE_HUNDRED_METERS = 0.1;

	private static Logger log = Logger.getLogger(GoogleMapsDisplayCleaner.class);
    
    private final DistanceMeasuringService distanceMeasuringService;
    
    public GoogleMapsDisplayCleaner() {
		this.distanceMeasuringService = new DistanceMeasuringService();
	}
    
	public List<FrontendResource> dedupe(List<FrontendResource> geocoded) {
        return dedupe(geocoded, null);
    }

    public List<FrontendResource> dedupe(List<FrontendResource> geocoded, FrontendResource selected) {      
        log.debug("Deduping collection with " + geocoded.size() + " items");
        final List<FrontendResource> deduped = Lists.newArrayList();
        if (selected != null && selected.getPlace() != null) {
            deduped.add(selected);
        }
        
        for (FrontendResource resource : geocoded) {
        	if (resource.getPlace() != null) {
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
            if (areSameOrOverlappingLocations(resource.getPlace(), candidiate.getPlace())) {
                log.debug("Rejected " + candidiate.getName() + " as it overlaps more recent item: " + resource.getPlace().getAddress() + " / " + candidiate.getPlace().getAddress());
                return true;
            }
        }
        return false;
    }

	private boolean areSameOrOverlappingLocations(Place here, Place there) {
		final double distanceBetweenHereAndThere = distanceMeasuringService.getDistanceBetween(here.getLatLong(), there.getLatLong());
		return distanceBetweenHereAndThere < ONE_HUNDRED_METERS;
	}
    
}
