package nz.co.searchwellington.tagging;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;






public class PlaceAutoTagger {
    
    Logger log = Logger.getLogger(PlaceAutoTagger.class);
    
    final String PLACES_TAG_NAME = "places";
        
    private ResourceRepository resourceDAO;
    private AutoTaggingService autoTaggingService;

    
    public PlaceAutoTagger(ResourceRepository resourceDAO, AutoTaggingService autoTaggingService) {
        this.resourceDAO = resourceDAO;
        this.autoTaggingService = autoTaggingService;
        
    }

    
    
    public void tag(Resource resource) {         
        Tag placesTag = resourceDAO.loadTagByName(PLACES_TAG_NAME);
        if (placesTag != null) {
            
            for (Tag placeTag : placesTag.getChildren()) {                          
                autoTag(resource, placeTag);            
            }
            
        } else {
            log.warn("Could not find places tag.");
        }
        
    }



    private void autoTag(Resource resource, Tag tag) {
        boolean headlineMatchesTag = resource.getName().toLowerCase().contains(tag.getDisplayName().toLowerCase());
        boolean BodyMatchesTag = resource.getDescription().toLowerCase().contains(tag.getDisplayName().toLowerCase());
        if ((headlineMatchesTag || BodyMatchesTag) && !autoTaggingService.alreadyHasTag(resource, tag)) {
            log.info("Auto tagging resource with '" + tag.getDisplayName() + "': " + resource.getName());
            resource.addTag(tag);            
        }
    }

}
