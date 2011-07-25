package nz.co.searchwellington.views;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.frontend.FrontendResource;

import org.apache.log4j.Logger;

public class GoogleMapsDisplayCleaner {
        
    private static Logger log = Logger.getLogger(GoogleMapsDisplayCleaner.class);
    
    public List<FrontendResource> dedupe(List<FrontendResource> geocoded) {
        return dedupe(geocoded, null);
    }

    public List<FrontendResource> dedupe(List<FrontendResource> geocoded, Resource selected) {      
        log.info("Deduping collection with " + geocoded.size() + " items");
        List<FrontendResource> deduped = new ArrayList<FrontendResource>();
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
            if (resource.getGeocode().isSameLocation(candidiate.getGeocode())) {
                log.debug("Rejected " + candidiate.getName() + " as it overlaps more recent item: " + resource.getGeocode().getAddress() + " / " + candidiate.getGeocode().getAddress());
                return true;
            }
        }
        return false;
    }
    
}
