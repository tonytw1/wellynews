package nz.co.searchwellington.tagging;

import java.util.HashSet;
import java.util.Set;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlaceAutoTagger {
    
	private static Logger log = Logger.getLogger(PlaceAutoTagger.class);
    
    final String PLACES_TAG_NAME = "places";
        
    private TagDAO tagDAO;
    
    @Autowired
    public PlaceAutoTagger(TagDAO tagDAO) {
        this.tagDAO = tagDAO;
    }
    
    public Set<Tag> suggestTags(Resource resource) {         
    	Set<Tag> suggestedTags = new HashSet<Tag>();
    	
        Tag placesTag = tagDAO.loadTagByName(PLACES_TAG_NAME);
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
        boolean bodyMatchesTag = resource.getDescription().toLowerCase().contains(tag.getDisplayName().toLowerCase());
        return headlineMatchesTag || bodyMatchesTag;        
    }

}
