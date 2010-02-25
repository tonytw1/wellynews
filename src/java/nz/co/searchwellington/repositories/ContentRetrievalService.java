package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.co.searchwellington.controllers.ShowBrokenDecisionService;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.solr.KeywordSearchService;

public class ContentRetrievalService {

	private ResourceRepository resourceDAO;
	private SolrContentRetrievalService solrContentRetrievalService;
	private KeywordSearchService keywordSearchService;
	private ShowBrokenDecisionService showBrokenDecisionService;
		

	public ContentRetrievalService(ResourceRepository resourceDAO,
			SolrContentRetrievalService solrContentRetrievalService,
			KeywordSearchService keywordSearchService,
			ShowBrokenDecisionService showBrokenDecisionService) {
		this.resourceDAO = resourceDAO;
		this.solrContentRetrievalService = solrContentRetrievalService;
		this.keywordSearchService = keywordSearchService;
		this.showBrokenDecisionService = showBrokenDecisionService;
	}

	public List<Resource> getAllWatchlists() {
		return solrContentRetrievalService.getAllWatchlists(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getGeocoded(int maxItems) {		
		return resourceDAO.getAllValidGeocoded(maxItems, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getAllPublishers(boolean mustHaveNewsitems) {	// TODO publishers can also have feeds
		return solrContentRetrievalService.getAllPublishers(showBrokenDecisionService.shouldShowBroken(), mustHaveNewsitems);
	}
	
	public List<PublisherContentCount> getAllPublishersWithNewsitemCounts(boolean mustHaveNewsitems) {
		return solrContentRetrievalService.getAllPublishersWithNewsitemCounts(showBrokenDecisionService.shouldShowBroken(), mustHaveNewsitems);
	}

	public List<Tag> getTopLevelTags() {
		return resourceDAO.getTopLevelTags();
	}

	public int getTaggedNewitemsCount(Tag tag) {
		return solrContentRetrievalService.getTaggedNewitemsCount(tag, showBrokenDecisionService.shouldShowBroken());
	}

	public int getCommentedNewsitemsForTagCount(Tag tag) {
		return solrContentRetrievalService.getCommentedNewsitemsForTagCount(tag, showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<Resource> getRecentCommentedNewsitemsForTag(Tag tag, int maxItems) {
		return solrContentRetrievalService.getRecentCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken(), maxItems);
	}

	public List<Resource> getTagWatchlist(Tag tag) {
		return solrContentRetrievalService.getTagWatchlist(tag, showBrokenDecisionService.shouldShowBroken());
	}
	
	public Date getLastLiveTimeForTag(Tag tag) {
		return resourceDAO.getLastLiveTimeForTag(tag);
	}

	public List<Resource> getTaggedFeeds(Tag tag) {
		return solrContentRetrievalService.getTaggedFeeds(tag, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getTaggedGeotaggedNewsitems(Tag tag, int maxItems) {
		return resourceDAO.getTaggedGeotaggedNewsitems(tag, maxItems, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Tag> getGeotaggedTags() {
		return solrContentRetrievalService.getGeotaggedTags(showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<Resource> getCommentedNewsitems(int maxItems, int startIndex) {
		return solrContentRetrievalService.getCommentedNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken(), true, startIndex);
	}

	public int getCommentedNewsitemsCount() {
		return solrContentRetrievalService.getCommentedNewsitemsCount(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Tag> getCommentedTags() {
		return solrContentRetrievalService.getCommentedTags(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getLatestNewsitems(int maxItems) {
		return solrContentRetrievalService.getLatestNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getLatestWebsites(int maxItems) {
		return solrContentRetrievalService.getLatestWebsites(maxItems, showBrokenDecisionService.shouldShowBroken());
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
		return solrContentRetrievalService.getRecentTwitteredNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getRecentedTwitteredNewsitemsForTag(int maxItems, Tag tag) {
		return solrContentRetrievalService.getRecentTwitteredNewsitemsForTag(maxItems, showBrokenDecisionService.shouldShowBroken(), tag);
	}

	public List<Resource> getOwnedBy(User loggedInUser, int maxItems) {
		return resourceDAO.getOwnedBy(loggedInUser, maxItems);
	}

	public List<Resource> getFeaturedSites() {
		final Tag featuredTag = resourceDAO.loadTagByName("featured");
		if (featuredTag != null) {
			return this.getTaggedWebsites(featuredTag, 10);
		}
		return null;
	}

	public List<Resource> getAllFeeds() {
		return solrContentRetrievalService.getAllFeeds(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getPublisherFeeds(Website publisher) {
		return solrContentRetrievalService.getPublisherFeeds(publisher, showBrokenDecisionService.shouldShowBroken());
	}

	public int getPublisherNewsitemsCount(Website publisher) {
		return solrContentRetrievalService.getPublisherNewsitemsCount(publisher, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getPublisherNewsitems(Website publisher, int maxItems, int startIndex) {
		return solrContentRetrievalService.getPublisherNewsitems(publisher, maxItems, showBrokenDecisionService.shouldShowBroken(), startIndex);
	}

	public List<Resource> getPublisherWatchlist(Website publisher) {
		return solrContentRetrievalService.getPublisherWatchlist(publisher, showBrokenDecisionService.shouldShowBroken());
	}

	public List<ArchiveLink> getArchiveMonths() {
		return solrContentRetrievalService.getArchiveMonths(showBrokenDecisionService.shouldShowBroken());
	}

	public Map<String, Integer> getArchiveStatistics() {
		return solrContentRetrievalService.getArchiveStatistics(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getCommentedNewsitemsForTag(Tag tag, int maxNewsitems, int startIndex) {
		return solrContentRetrievalService.getCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken(), maxNewsitems, startIndex);
	}

	public List<Resource> getNewsitemsForMonth(Date month) {
		return solrContentRetrievalService.getNewsitemsForMonth(month, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getTaggedWebsites(HashSet<Tag> tags, int maxItems) {
		return solrContentRetrievalService.getTaggedWebsites(tags, showBrokenDecisionService.shouldShowBroken(), maxItems);
	}

	public List<Resource> getTaggedNewsitems(HashSet<Tag> tags, int maxItems) {
		return solrContentRetrievalService.getTaggedNewsitems(tags, showBrokenDecisionService.shouldShowBroken(), 0, maxItems);
	}
	
	public List<Resource> getTaggedNewsitems(Tag tag, int startIndex, int maxNewsitems) {
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		return solrContentRetrievalService.getTaggedNewsitems(tags, showBrokenDecisionService.shouldShowBroken(), startIndex, maxNewsitems);
	}
	
	
	public List<Resource> getTaggedWebsites(Tag tag, int maxItems) {
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		return solrContentRetrievalService.getTaggedWebsites(tags, showBrokenDecisionService.shouldShowBroken(), maxItems);
	}
	
}
