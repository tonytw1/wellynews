package nz.co.searchwellington.tagging;

import java.util.Set;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.UserRepository;

import org.apache.log4j.Logger;

public class AutoTaggingService {
	
	private static Logger log = Logger.getLogger(AutoTaggingService.class);
	
	private static final String AUTOTAGGER_PROFILE_NAME = "autotagger";
   
	private PlaceAutoTagger placeAutoTagger;
	private TagHintAutoTagger tagHintAutoTagger;
	private HandTaggingDAO handTaggingDAO;
	private UserRepository userDAO;
	
	public AutoTaggingService(PlaceAutoTagger placeAutoTagger, TagHintAutoTagger tagHintAutoTagger, HandTaggingDAO handTaggingDAO, UserRepository userDAO) {
		this.placeAutoTagger = placeAutoTagger;
		this.tagHintAutoTagger = tagHintAutoTagger;
		this.handTaggingDAO = handTaggingDAO;
		this.userDAO = userDAO;
	}
	
	public void autotag(Resource resource) {
		final User autotaggerUser = userDAO.getUserByProfileName(AUTOTAGGER_PROFILE_NAME);
		if (autotaggerUser == null) {
			log.warn("Could not find auto tagger user: " + AUTOTAGGER_PROFILE_NAME);
			return;
		}
		
		Set<Tag> suggestedTags = placeAutoTagger.suggestTags(resource);
		suggestedTags.addAll(tagHintAutoTagger.suggestTags(resource));

		log.info("Suggested tags for '" + resource.getName() + "' are: " + suggestedTags.toString());
		if (!suggestedTags.isEmpty()) {
			handTaggingDAO.setUsersTagVotesForResource(resource, autotaggerUser, suggestedTags);
		}
	}

}
