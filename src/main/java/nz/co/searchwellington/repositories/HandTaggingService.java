package nz.co.searchwellington.repositories;

import java.util.List;

import org.apache.log4j.Logger;

import nz.co.searchwellington.model.HandTagging;
import nz.co.searchwellington.model.Tag;

public class HandTaggingService {
	
	private static Logger log = Logger.getLogger(HandTaggingService.class);

	private HandTaggingDAO handTaggingDao;
	private FrontendContentUpdater frontendContentUpdater;	

	public HandTaggingService(HandTaggingDAO handTaggingDao, FrontendContentUpdater frontendContentUpdater) {
		this.handTaggingDao = handTaggingDao;
		this.frontendContentUpdater = frontendContentUpdater;
	}
	
	public void clearTaggingsForTag(Tag tag) {
		log.info("Cleaning tagging votes for tag: " + tag.getName());
		List<HandTagging> votesForTag = handTaggingDao.getVotesForTag(tag);
		log.info(votesForTag.size() + " votes will needs to be cleared and the frontend resources updated.");
		for (HandTagging handTagging : votesForTag) {
			handTaggingDao.delete(handTagging);
			frontendContentUpdater.update(handTagging.getResource());
		}
	}

}
