package nz.co.searchwellington.tagging;

import java.util.Set;

import org.apache.log4j.Logger;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

public class AutoTaggingService {
	
	 Logger log = Logger.getLogger(AutoTaggingService.class);
	    

	private ImpliedTagService impliedTagService;
	private PlaceAutoTagger placeAutoTagger;
	
	
	public AutoTaggingService(ImpliedTagService impliedTagService, PlaceAutoTagger placeAutoTagger) {
		this.impliedTagService = impliedTagService;
		this.placeAutoTagger = placeAutoTagger;
	}

	
	public void autotag(Resource resource) {
		Set<Tag> suggestedTags = placeAutoTagger.suggestTags(resource);
		for (Tag tag : suggestedTags) {
			if (!impliedTagService.alreadyHasTag(resource, tag)) {
				resource.addTag(tag);
				log.info("Autotag resource '" + resource.getName() + "' with " + tag.getName());
			}
		}
	}

}
