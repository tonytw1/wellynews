package nz.co.searchwellington.tagging;

import java.util.HashSet;
import java.util.Set;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;

public class PlaceAutoTagger {
    
    Logger log = Logger.getLogger(PlaceAutoTagger.class);
    
    final String PLACES_TAG_NAME = "places";
        
    private ResourceRepository resourceDAO;
    
    public PlaceAutoTagger(ResourceRepository resourceDAO, ImpliedTagService autoTaggingService) {
        this.resourceDAO = resourceDAO;       
    }
    
    public Set<Tag> suggestTags(Resource resource) {         
    	Set<Tag> suggestedTags = new HashSet<Tag>();
    	
        Tag placesTag = resourceDAO.loadTagByName(PLACES_TAG_NAME);
        if (placesTag != null) {            
            for (Tag placeTag : placesTag.getChildren()) {                          
                if (checkForMatchingTag(resource, placeTag)) {
                	log.info("Suggesting tag '" + placeTag.getDisplayName() + "' for resource: " + resource.getName());
                	suggestedTags.add(placeTag);
                }
            }            
        } else {
            log.warn("Could not find places tag.");
        }
        return suggestedTags;
        
    }

    private boolean checkForMatchingTag(Resource resource, Tag tag) {
        boolean headlineMatchesTag = resource.getName().toLowerCase().contains(tag.getDisplayName().toLowerCase());
        boolean BodyMatchesTag = resource.getDescription().toLowerCase().contains(tag.getDisplayName().toLowerCase());
        return headlineMatchesTag || BodyMatchesTag;        
    }

}
