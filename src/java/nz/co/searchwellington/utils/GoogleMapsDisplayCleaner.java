package nz.co.searchwellington.utils;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Resource;

import org.apache.log4j.Logger;

public class GoogleMapsDisplayCleaner {
        
    Logger log = Logger.getLogger(GoogleMapsDisplayCleaner.class);
        
    public List<Resource> dedupe(List<Resource> geocoded) {
        return dedupe(geocoded, null);
    }

    public List<Resource> dedupe(List<Resource> geocoded, Resource selected) {      
        log.info("Deduping collection with " + geocoded.size() + " items");
        List<Resource> deduped = new ArrayList<Resource>();
        
        if (selected != null && selected.getGeocode() != null) {
            deduped.add(selected);
        }
        
        for (Resource resource : geocoded) {
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

    
    private boolean listAlreadyContainsResourceWithThisLocation(List<Resource> deduped, Resource candidiate) {
        for (Resource resource : deduped) {
            if (resource.getGeocode().isSameLocation(candidiate.getGeocode())) {
                log.info("Rejected " + candidiate.getName() + " as it overlaps more recent item: " + resource.getGeocode().getAddress() + " / " + candidiate.getGeocode().getAddress());
                return true;
            }
        }
        return false;
    }


}
