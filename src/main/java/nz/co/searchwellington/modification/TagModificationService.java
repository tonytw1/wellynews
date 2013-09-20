package nz.co.searchwellington.modification;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.HandTaggingService;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagModificationService {
	
	private static Logger log = Logger.getLogger(TagModificationService.class);

    private TagDAO tagDAO;
    private HandTaggingService handTaggingService;
        
	@Autowired
	public TagModificationService(TagDAO tagDAO, HandTaggingService handTaggingService) {
		this.tagDAO = tagDAO;
		this.handTaggingService = handTaggingService;
	}
	
	public void updateTagParent(Tag editTag, Tag parentTag) {
		log.debug("Setting parent tag to: " + parentTag.getName());	
		editTag.setParent(parentTag);
		// TODO should now reindex all resources effected by this.		
	}
	
	public void deleteTag(Tag tag) {
		log.info("Deleting tag " + tag.getName());
		handTaggingService.clearTaggingsForTag(tag);                    
		if (tag.getParent() != null) {
		    tag.getParent().getChildren().remove(tag);
		}
		tagDAO.deleteTag(tag);
	}
	
}
