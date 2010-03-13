package nz.co.searchwellington.tagging;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

public class ImpliedTagService {
	   	
	private TaggingReturnsOfficerService taggingReturnsOfficerService;
	
	public ImpliedTagService(TaggingReturnsOfficerService taggingReturnsOfficerService) {
		this.taggingReturnsOfficerService = taggingReturnsOfficerService;
	}

   
   public boolean alreadyHasTag(Resource resource, Tag tag) {	// TODO should look at this users votes only
	   boolean isNewsitemWhosPublisherAlreadyHasThisTag = resource.getType().equals("N") && ((Newsitem) resource).getPublisher() != null && taggingReturnsOfficerService.getHandTagsForResource(((Newsitem) resource).getPublisher()).contains(tag);
	   boolean resourceAlreadyHasTag = taggingReturnsOfficerService.getHandTagsForResource(resource).contains(tag) || isNewsitemWhosPublisherAlreadyHasThisTag;
	   return resourceAlreadyHasTag;
   }

    
}
