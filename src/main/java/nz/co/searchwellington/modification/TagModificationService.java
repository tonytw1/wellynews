package nz.co.searchwellington.modification;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;

public class TagModificationService {
	
	static Logger log = Logger.getLogger(TagModificationService.class);

    private TagDAO tagDAO;
    private ContentUpdateService contentUpdateService;
    private HandTaggingDAO handTaggingDAO;
    
    
	public TagModificationService(TagDAO tagDAO,
			ContentUpdateService contentUpdateService,
			HandTaggingDAO handTaggingDAO) {
		this.tagDAO = tagDAO;
		this.contentUpdateService = contentUpdateService;
		this.handTaggingDAO = handTaggingDAO;
	}
	

	public void updateTagParent(Tag editTag, Tag parentTag) {
		log.info("Setting parent tag to: " + parentTag.getName());	
		editTag.setParent(parentTag);
		// TODO should now reindex all resources effected by this.		
	}

	public void deleteTag(Tag tag) {
		log.info("Deleting tag " + tag.getName());
		removeDeletedTagFromResources(tag);                    
		if (tag.getParent() != null) {
		    tag.getParent().getChildren().remove(tag);
		}
		tagDAO.deleteTag(tag);
	}

	
	private void removeDeletedTagFromResources(Tag tag) {
		List<Resource> taggedResources = handTaggingDAO.getResourcesWithTag(tag);
		handTaggingDAO.clearTaggingsForTag(tag);
		log.info("Tag to be deleted has " + taggedResources.size() + " resources.");
		for (Resource resource : taggedResources) {
		    contentUpdateService.update(resource);	// TODO really only needs a solr update, not a full database write
		}
	}
	
}
