package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.repositories.ResourceRepository;

public class RelatedTagsService {
		
	private static final int MAX_WEBSITES = 100;
	
	
    Logger log = Logger.getLogger(RelatedTagsService.class);

	
	private ResourceRepository resourceDAO;

	
	public RelatedTagsService(ResourceRepository resourceDAO) {
		this.resourceDAO = resourceDAO;
	}


    public List<TagContentCount> getRelatedTagLinks(Tag tag, boolean showBroken) {
        List<Tag> relatedTags = resourceDAO.getRelatedLinksForTag(tag, showBroken);
        List<TagContentCount> relatedTagLinks = new ArrayList<TagContentCount>();
        for (Tag relatedTag : relatedTags) {                        
            boolean relatedTagIsNotAncestor = !tag.getAncestors().contains(relatedTag);
			if (relatedTagIsNotAncestor) {
				try {
					// TODO needs a count only method				
					int relatedItemCount = resourceDAO.getTaggedWebsites(new HashSet<Tag>(Arrays.asList(tag, relatedTag)), showBroken, MAX_WEBSITES).size();
					// TODO merge these calls to get a speed up.
					relatedItemCount = relatedItemCount + resourceDAO.getTaggedNewsitems(new HashSet<Tag>(Arrays.asList(tag, relatedTag)), showBroken, MAX_WEBSITES).size();
					relatedTagLinks.add(new TagContentCount(relatedTag, relatedItemCount));
				} catch (IOException e) {
					log.warn("Exception while going item count for related tag " + tag, e);
				}
            }
        }
        Collections.sort(relatedTagLinks);
        return relatedTagLinks;
    }

	
}
