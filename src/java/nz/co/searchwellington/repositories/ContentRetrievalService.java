package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.controllers.ShowBrokenDecisionService;
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.solr.KeywordSearchService;

public class ContentRetrievalService {

	private ResourceRepository resourceDAO;
	private KeywordSearchService keywordSearchService;
	private ShowBrokenDecisionService showBrokenDecisionService;
	

	public ContentRetrievalService(ResourceRepository resourceDAO, KeywordSearchService keywordSearchService, ShowBrokenDecisionService showBrokenDecisionService) {
		this.resourceDAO = resourceDAO;
        this.keywordSearchService = keywordSearchService;
		this.showBrokenDecisionService = showBrokenDecisionService;
	}

	public List<Resource> getAllWatchlists() {
		return resourceDAO.getAllWatchlists(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getGeocoded(int maxItems) {
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

	public List<Tag> getGeotaggedTags() {
		return resourceDAO.getGeotaggedTags(showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<Resource> getCommentedNewsitems(int maxItems, int startIndex) {
		return resourceDAO.getCommentedNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken(), true, startIndex);
	}

	public int getCommentedNewsitemsCount() {
		return resourceDAO.getCommentedNewsitemsCount(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Tag> getCommentedTags() {
		return resourceDAO.getCommentedTags(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getLatestNewsitems(int maxItems) {
		return resourceDAO.getLatestNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getLatestWebsites(int maxItems) {
		return resourceDAO.getLatestWebsites(maxItems, showBrokenDecisionService.shouldShowBroken());
	}

	public List<TagContentCount> getKeywordSearchFacets(String keywords) {
		return keywordSearchService.getKeywordSearchFacets(keywords, showBrokenDecisionService.shouldShowBroken(), null);
	}

	public List<Resource> getWebsitesMatchingKeywords(String keywords, Tag tag) {
		return keywordSearchService.getWebsitesMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken(), tag);
	}

	public List<Resource> getNewsitemsMatchingKeywords(String keywords, Tag tag) {
		return keywordSearchService.getNewsitemsMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken(), tag);
	}
	
	public List<Resource> getRecentedTwitteredNewsitems(int maxItems) {
		return resourceDAO.getRecentTwitteredNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getRecentedTwitteredNewsitemsForTag(int maxItems, Tag tag) {
		return resourceDAO.getRecentTwitteredNewsitemsForTag(maxItems, showBrokenDecisionService.shouldShowBroken(), tag);
	}

	public List<Resource> getOwnedBy(User loggedInUser, int maxItems) {
		return resourceDAO.getOwnedBy(loggedInUser, maxItems);
	}

	public List<Resource> getFeaturedSites() {
		final Tag featuredTag = resourceDAO.loadTagByName("featured");
		if (featuredTag != null) {         
			return resourceDAO.getTaggedWebsites(featuredTag,  showBrokenDecisionService.shouldShowBroken(), 10);
		}
		return null;
	}
	
}
