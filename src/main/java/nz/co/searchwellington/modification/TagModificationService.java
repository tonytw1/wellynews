package nz.co.searchwellington.modification;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;

public class TagModificationService {
	
	static Logger log = Logger.getLogger(TagModificationService.class);

    private TagDAO tagDAO;
    private HandTaggingDAO handTaggingDAO;
        
	public TagModificationService(TagDAO tagDAO,
			HandTaggingDAO handTaggingDAO) {
		this.tagDAO = tagDAO;
		this.handTaggingDAO = handTaggingDAO;
	}
	
	public void updateTagParent(Tag editTag, Tag parentTag) {
		log.info("Setting parent tag to: " + parentTag.getName());	
		editTag.setParent(parentTag);
		// TODO should now reindex all resources effected by this.		
	}
	
	public void deleteTag(Tag tag) {
		log.info("Deleting tag " + tag.getName());
		handTaggingDAO.clearTaggingsForTag(tag);                    
		if (tag.getParent() != null) {
		    tag.getParent().getChildren().remove(tag);
		}
		tagDAO.deleteTag(tag);
	}
	
}
