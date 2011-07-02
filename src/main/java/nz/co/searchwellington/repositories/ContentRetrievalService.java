package nz.co.searchwellington.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.ShowBrokenDecisionService;
import nz.co.searchwellington.feeds.DiscoveredFeedRepository;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.solr.KeywordSearchService;

public class ContentRetrievalService {
	
	final protected int MAX_NEWSITEMS_TO_SHOW = 30;
        
	private ResourceRepository resourceDAO;
	private KeywordSearchService keywordSearchService;
	private ShowBrokenDecisionService showBrokenDecisionService;
	private TagDAO tagDAO;
	private RelatedTagsService relatedTagsService;
	private DiscoveredFeedRepository discoveredFeedsDAO;
	private SolrBackedResourceDAO solrBackedResourceDAO;
	
	public ContentRetrievalService(ResourceRepository resourceDAO,
			KeywordSearchService keywordSearchService,
			ShowBrokenDecisionService showBrokenDecisionService, TagDAO tagDAO,
			RelatedTagsService relatedTagsService,
			DiscoveredFeedRepository discoveredFeedsDAO, SolrBackedResourceDAO solrBackedResourceDAO) {
		this.resourceDAO = resourceDAO;
		this.keywordSearchService = keywordSearchService;
		this.showBrokenDecisionService = showBrokenDecisionService;
		this.tagDAO = tagDAO;
		this.relatedTagsService = relatedTagsService;
		this.discoveredFeedsDAO = discoveredFeedsDAO;
		this.solrBackedResourceDAO = solrBackedResourceDAO;
	}
	
	public List<FrontendResource> getAllWatchlists() {
		return solrBackedResourceDAO.getAllWatchlists(showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getGeocoded(int startIndex, int maxItems) {		
		return solrBackedResourceDAO.getValidGeotagged(startIndex, maxItems, showBrokenDecisionService.shouldShowBroken());
	}
	
	public int getGeotaggedCount() {
		return solrBackedResourceDAO.getGeotaggedCount(showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<FrontendResource> getAllPublishers() {
		return solrBackedResourceDAO.getAllPublishers(showBrokenDecisionService.shouldShowBroken(), false);
	}
	
	public List<Tag> getTopLevelTags() {
		return tagDAO.getTopLevelTags();
	}

	public int getTaggedNewitemsCount(Tag tag) {
		return solrBackedResourceDAO.getTaggedNewitemsCount(tag, showBrokenDecisionService.shouldShowBroken());
	}

	public int getCommentedNewsitemsForTagCount(Tag tag) {
		return solrBackedResourceDAO.getCommentedNewsitemsForTagCount(tag, showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<FrontendResource> getRecentCommentedNewsitemsForTag(Tag tag, int maxItems) {
		return solrBackedResourceDAO.getRecentCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken(), maxItems);
	}

	public List<FrontendResource> getTagWatchlist(Tag tag) {
		return solrBackedResourceDAO.getTagWatchlist(tag, showBrokenDecisionService.shouldShowBroken());
	}
	
	public Date getLastLiveTimeForTag(Tag tag) {
		return solrBackedResourceDAO.getLastLiveTimeForTag(tag);
	}

	public List<FrontendResource> getTaggedFeeds(Tag tag) {
		return solrBackedResourceDAO.getTaggedFeeds(tag, showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getTaggedGeotaggedNewsitems(Tag tag, int maxItems) {
		return solrBackedResourceDAO.getTaggedGeotaggedNewsitems(tag, maxItems, showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<FrontendResource> getNewsitemsNear(double latitude, double longitude, int radius) {
		return solrBackedResourceDAO.getGeotaggedNewsitemsNear(latitude, longitude, radius, showBrokenDecisionService.shouldShowBroken(), MAX_NEWSITEMS_TO_SHOW);
	}
	
	public List<Tag> getGeotaggedTags() {
		return solrBackedResourceDAO.getGeotaggedTags(showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<FrontendResource> getCommentedNewsitems(int maxItems, int startIndex) {
		return solrBackedResourceDAO.getCommentedNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken(), true, startIndex);
	}

	public int getCommentedNewsitemsCount() {
		return solrBackedResourceDAO.getCommentedNewsitemsCount(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Tag> getCommentedTags() {
		return solrBackedResourceDAO.getCommentedTags(showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getLatestNewsitems(int maxItems) {
		return solrBackedResourceDAO.getLatestNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getLatestWebsites(int maxItems) {
		return solrBackedResourceDAO.getLatestWebsites(maxItems, showBrokenDecisionService.shouldShowBroken());
	}

	public List<TagContentCount> getKeywordSearchFacets(String keywords) {
		return keywordSearchService.getKeywordSearchFacets(keywords, showBrokenDecisionService.shouldShowBroken(), null);
	}

	public List<FrontendResource> getWebsitesMatchingKeywords(String keywords, Tag tag) {
		return keywordSearchService.getWebsitesMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken(), tag);
	}

	public List<FrontendResource> getNewsitemsMatchingKeywords(String keywords, Tag tag) {
		return keywordSearchService.getNewsitemsMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken(), tag);
	}
	
	public List<FrontendResource> getRecentedTwitteredNewsitems() {
		return solrBackedResourceDAO.getRecentTwitteredNewsitems(MAX_NEWSITEMS_TO_SHOW, showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getRecentedTwitteredNewsitemsForTag(int maxItems, Tag tag) {
		return solrBackedResourceDAO.getRecentTwitteredNewsitemsForTag(maxItems, showBrokenDecisionService.shouldShowBroken(), tag);
	}

	public List<FrontendResource> getFeaturedSites() {
		final Tag featuredTag = tagDAO.loadTagByName("featured");
		if (featuredTag != null) {
			return this.getTaggedWebsites(featuredTag, 10);
		}
		return null;
	}

	public List<FrontendResource> getAllFeeds() {
		return solrBackedResourceDAO.getAllFeeds(showBrokenDecisionService.shouldShowBroken(), false);
	}
	
	public Feed getFeedByUrlWord(String feedUrlWords) {
		return resourceDAO.loadFeedByUrlWords(feedUrlWords);
	}
	
	public List<FrontendResource> getAllFeedsOrderByLatestItemDate() {
		return solrBackedResourceDAO.getAllFeeds(showBrokenDecisionService.shouldShowBroken(), true);
	}
	
	public List<FrontendResource> getPublisherFeeds(Website publisher) {
		return solrBackedResourceDAO.getPublisherFeeds(publisher, showBrokenDecisionService.shouldShowBroken());
	}

	public int getPublisherNewsitemsCount(Website publisher) {
		return solrBackedResourceDAO.getPublisherNewsitemsCount(publisher, showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getPublisherNewsitems(Website publisher, int maxItems, int startIndex) {
		return solrBackedResourceDAO.getPublisherNewsitems(publisher, maxItems, showBrokenDecisionService.shouldShowBroken(), startIndex);
	}

	public List<FrontendResource> getPublisherWatchlist(Website publisher) {
		return solrBackedResourceDAO.getPublisherWatchlist(publisher, showBrokenDecisionService.shouldShowBroken());
	}

	public List<ArchiveLink> getArchiveMonths() {
		return solrBackedResourceDAO.getArchiveMonths(showBrokenDecisionService.shouldShowBroken());
	}

	public Map<String, Integer> getArchiveStatistics() {
		return solrBackedResourceDAO.getArchiveStatistics(showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getCommentedNewsitemsForTag(Tag tag, int maxNewsitems, int startIndex) {
		return solrBackedResourceDAO.getCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken(), maxNewsitems, startIndex);
	}

	public List<FrontendResource> getNewsitemsForMonth(Date month) {
		return solrBackedResourceDAO.getNewsitemsForMonth(month, showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getTaggedWebsites(HashSet<Tag> tags, int maxItems) {
		return solrBackedResourceDAO.getTaggedWebsites(tags, showBrokenDecisionService.shouldShowBroken(), maxItems);
	}

	public List<FrontendResource> getTaggedNewsitems(HashSet<Tag> tags, int maxItems) {
		return solrBackedResourceDAO.getTaggedNewsitems(tags, showBrokenDecisionService.shouldShowBroken(), 0, maxItems);
	}
	
	public List<FrontendResource> getTaggedNewsitems(Tag tag, int startIndex, int maxNewsitems) {
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		return solrBackedResourceDAO.getTaggedNewsitems(tags, showBrokenDecisionService.shouldShowBroken(), startIndex, maxNewsitems);
	}
	
	
	public List<FrontendResource> getTaggedWebsites(Tag tag, int maxItems) {
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		return solrBackedResourceDAO.getTaggedWebsites(tags, showBrokenDecisionService.shouldShowBroken(), maxItems);
	}

	public List<FrontendResource> getBrokenSites() {
		return solrBackedResourceDAO.getBrokenSites();	
	}

	public List<FrontendResource> getPublisherTagCombinerNewsitems(Website publisher, Tag tag, int maxNewsitems) {
		return solrBackedResourceDAO.getPublisherTagCombinerNewsitems(publisher, tag, showBrokenDecisionService.shouldShowBroken(), maxNewsitems);
	}
	
	public List<FrontendResource> getPublisherTagCombinerNewsitems(String publisherUrlWords, String tagName, int maxNewsitems) {
		Website publisher = resourceDAO.getPublisherByUrlWords(publisherUrlWords);
		Tag tag = tagDAO.loadTagByName(tagName);
		if (publisher != null && tag != null) {
			return this.getPublisherTagCombinerNewsitems(publisher, tag, maxNewsitems);
		}
		return null;		
	}
	
	public List<FrontendResource> getRecentlyChangedWatchlistItems() {
		return solrBackedResourceDAO.getAllWatchlists(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Tag> getFeedworthyTags() {
		List<Tag> feedworthy = new ArrayList<Tag>();
		for (TagContentCount tagContentCount : relatedTagsService.getFeedworthyTags(showBrokenDecisionService.shouldShowBroken())) {
			feedworthy.add(tagContentCount.getTag());
		}		
		return feedworthy;
	}

	public List<DiscoveredFeed> getDiscoveredFeeds() {
		return discoveredFeedsDAO.getAllNonCommentDiscoveredFeeds();
	}

	public List<Resource> getOwnedBy(User loggedInUser, int maxItems) {
		return resourceDAO.getOwnedBy(loggedInUser, maxItems);	// TODO push to solr for profile page usages.
	}
	
	public List<FrontendResource> getTaggedBy(User user, int maxItems) {
		return solrBackedResourceDAO.getHandTaggingsForUser(user, showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<String> getTagNamesStartingWith(String q) {
		return tagDAO.getTagNamesStartingWith(q);
	}

	public List<String> getPublisherNamesByStartingLetters(String q) {
		return resourceDAO.getPublisherNamesByStartingLetters(q);
	}

	public int getOwnedByCount(User loggedInUser) {
		return resourceDAO.getOwnedByUserCount(loggedInUser);
	}

	public Newsitem getNewsPage(String pathInfo) {
		return solrBackedResourceDAO.getNewspage(pathInfo, showBrokenDecisionService.shouldShowBroken());
	}
	
}
