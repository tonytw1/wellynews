package nz.co.searchwellington.tagging;

import java.util.Set;

import org.apache.log4j.Logger;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.UserRepository;

public class AutoTaggingService {
	
	static Logger log = Logger.getLogger(AutoTaggingService.class);
	
	private static final String AUTOTAGGER_PROFILE_NAME = "autotagger";
   
	private ImpliedTagService impliedTagService;
	private PlaceAutoTagger placeAutoTagger;
	private TagHintAutoTagger tagHintAutoTagger;
	private HandTaggingDAO handTaggingDAO;
	private UserRepository userDAO;
	
	public AutoTaggingService(ImpliedTagService impliedTagService, PlaceAutoTagger placeAutoTagger, TagHintAutoTagger tagHintAutoTagger, HandTaggingDAO handTaggingDAO, UserRepository userDAO) {
		this.impliedTagService = impliedTagService;
		this.placeAutoTagger = placeAutoTagger;
		this.tagHintAutoTagger = tagHintAutoTagger;
		this.handTaggingDAO = handTaggingDAO;
		this.userDAO = userDAO;
	}

	
	public void autotag(Resource resource) {
		User autotaggerUser = userDAO.getUserByProfileName(AUTOTAGGER_PROFILE_NAME);
		if (autotaggerUser == null) {
			log.warn("Could not find auto tagger user: " + AUTOTAGGER_PROFILE_NAME);
			return;
		}
		
		Set<Tag> suggestedTags = placeAutoTagger.suggestTags(resource);
		log.info("Suggested tags for '" + resource.getName() + "' are: " + suggestedTags.toString());
		suggestedTags.addAll(tagHintAutoTagger.suggestTags(resource));
		for (Tag tag : suggestedTags) {
			if (!impliedTagService.alreadyHasTag(resource, tag)) {
				log.info("Autotagging resource '" + resource.getName() + "' with " + tag.getName());
				handTaggingDAO.addTag(autotaggerUser, tag, resource);
			} else {
				log.info("Resource already has tag '" + tag.getName() + "'; ignoring");
			}
		}
	}

}
