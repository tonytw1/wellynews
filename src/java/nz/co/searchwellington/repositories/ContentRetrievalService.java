package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.controllers.ShowBrokenDecisionService;
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

public class ContentRetrievalService {

	private ResourceRepository resourceDAO;
	private ShowBrokenDecisionService showBrokenDecisionService;

	public ContentRetrievalService(ResourceRepository resourceDAO, ShowBrokenDecisionService showBrokenDecisionService) {
		this.resourceDAO = resourceDAO;
		this.showBrokenDecisionService = showBrokenDecisionService;
	}

	public List<Resource> getAllWatchlists() {
		return resourceDAO.getAllWatchlists(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getAllValidGeocoded(int maxItems) {
		return resourceDAO.getAllValidGeocoded(maxItems, showBrokenDecisionService.shouldShowBroken());
	}

	public List<PublisherContentCount> getAllPublishersWithNewsitemCounts(boolean mustHaveNewsitems) {
		return resourceDAO.getAllPublishers(showBrokenDecisionService.shouldShowBroken(), mustHaveNewsitems);
	}

	public List<Tag> getTopLevelTags() {
		return resourceDAO.getTopLevelTags();
	}

	public int getTaggedNewitemsCount(Tag tag) {
		return resourceDAO.getTaggedNewitemsCount(tag, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getTaggedNewsitems(Tag tag, int startIndex, int maxNewsitems) {
		return resourceDAO.getTaggedNewsitems(tag, showBrokenDecisionService.shouldShowBroken(), startIndex, maxNewsitems);
	}

	public List<Resource> getTaggedWebsites(Tag tag, int maxItems) {
		return resourceDAO.getTaggedWebsites(tag, showBrokenDecisionService.shouldShowBroken(), maxItems);
	}
	
	public int getCommentedNewsitemsForTagCount(Tag tag) {
		return resourceDAO.getCommentedNewsitemsForTagCount(tag, showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<Resource> getRecentCommentedNewsitemsForTag(Tag tag, int maxItems) {
		return resourceDAO.getRecentCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken(), maxItems);
	}

	public List<Resource> getTagWatchlist(Tag tag) {
		return resourceDAO.getTagWatchlist(tag, showBrokenDecisionService.shouldShowBroken());
	}
	
	public Date getLastLiveTimeForTag(Tag tag) {
		return resourceDAO.getLastLiveTimeForTag(tag);
	}

	public List<Resource> getTaggedFeeds(Tag tag) {
		return resourceDAO.getTaggedFeeds(tag, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getTaggedGeotaggedNewsitems(Tag tag, int maxItems) {
		return resourceDAO.getTaggedGeotaggedNewsitems(tag, maxItems, showBrokenDecisionService.shouldShowBroken());
	}
		
}
