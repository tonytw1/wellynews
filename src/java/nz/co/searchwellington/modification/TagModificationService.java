package nz.co.searchwellington.modification;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;

public class TagModificationService {
	
	static Logger log = Logger.getLogger(TagModificationService.class);

    private TagDAO tagDAO;
    private ResourceRepository resourceDAO;
    private ContentUpdateService contentUpdateService;
        
	public TagModificationService(TagDAO tagDAO,
			ResourceRepository resourceDAO,
			ContentUpdateService contentUpdateService) {
		this.tagDAO = tagDAO;
		this.resourceDAO = resourceDAO;
		this.contentUpdateService = contentUpdateService;
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
		List<Resource> taggedResources = resourceDAO.getResourcesWithTag(tag);            
		log.info("Tag to be deleted has " + taggedResources.size() + " resources.");
		for (Resource resource : taggedResources) {            	
			log.info("Removing tag from: " + resource.getName());
		    resource.getRemoveTag(tag);
		    contentUpdateService.update(resource, false);
		}
	}
	
}
