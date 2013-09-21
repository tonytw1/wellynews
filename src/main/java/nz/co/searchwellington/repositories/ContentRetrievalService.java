package nz.co.searchwellington.repositories;

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
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendTag;
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchBackedResourceDAO;
import nz.co.searchwellington.repositories.solr.KeywordSearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.LatLong;

import com.google.common.collect.Lists;

@Component
public class ContentRetrievalService {
	
	public static final int MAX_NEWSITEMS_TO_SHOW = 30;
        
	private HibernateResourceDAO resourceDAO;
	private KeywordSearchService keywordSearchService;
	private ShowBrokenDecisionService showBrokenDecisionService;
	private TagDAO tagDAO;
	private RelatedTagsService relatedTagsService;
	private DiscoveredFeedRepository discoveredFeedsDAO;
	private ElasticSearchBackedResourceDAO elasticSearchBackedResourceDAO;
	
	@Autowired
	public ContentRetrievalService(HibernateResourceDAO resourceDAO,
			KeywordSearchService keywordSearchService,
			ShowBrokenDecisionService showBrokenDecisionService, TagDAO tagDAO,
			RelatedTagsService relatedTagsService,
			DiscoveredFeedRepository discoveredFeedsDAO,
			ElasticSearchBackedResourceDAO solrBackedResourceDAO) {
		this.resourceDAO = resourceDAO;
		this.keywordSearchService = keywordSearchService;
		this.showBrokenDecisionService = showBrokenDecisionService;
		this.tagDAO = tagDAO;
		this.relatedTagsService = relatedTagsService;
		this.discoveredFeedsDAO = discoveredFeedsDAO;
		this.elasticSearchBackedResourceDAO = solrBackedResourceDAO;
	}
	
	public List<FrontendResource> getAllWatchlists() {
		return elasticSearchBackedResourceDAO.getAllWatchlists(showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<FrontendResource> getGeocoded(int startIndex, int maxItems) {		
		return elasticSearchBackedResourceDAO.getGeotagged(startIndex, maxItems, showBrokenDecisionService.shouldShowBroken());
	}
	
	public long getGeotaggedCount() {
		return elasticSearchBackedResourceDAO.getGeotaggedCount(showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<PublisherContentCount> getAllPublishers() {
		return elasticSearchBackedResourceDAO.getAllPublishers(showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<Tag> getTopLevelTags() {
		return tagDAO.getTopLevelTags();
	}

	public long getTaggedNewitemsCount(Tag tag) {
		return elasticSearchBackedResourceDAO.getTaggedNewitemsCount(tag, showBrokenDecisionService.shouldShowBroken());
	}

	public int getCommentedNewsitemsForTagCount(Tag tag) {
		return elasticSearchBackedResourceDAO.getCommentedNewsitemsForTagCount(tag, showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<FrontendResource> getRecentCommentedNewsitemsForTag(Tag tag, int maxItems) {
		return elasticSearchBackedResourceDAO.getRecentCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken(), maxItems);
	}

	public List<FrontendResource> getTagWatchlist(Tag tag) {
		return elasticSearchBackedResourceDAO.getTagWatchlist(tag, showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<FrontendResource> getTaggedFeeds(Tag tag) {
		return elasticSearchBackedResourceDAO.getTaggedFeeds(tag, showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getTaggedGeotaggedNewsitems(Tag tag, int maxItems) {
		return elasticSearchBackedResourceDAO.getTaggedGeotaggedNewsitems(tag, maxItems, showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<FrontendResource> getNewsitemsNear(LatLong latLong, double radius, int startIndex, int maxNewsitems) {
		return elasticSearchBackedResourceDAO.getGeotaggedNewsitemsNear(latLong, radius, showBrokenDecisionService.shouldShowBroken(), startIndex, maxNewsitems);
	}
	
	public Map<Double, Long> getNewsitemsNearDistanceFacet(LatLong latLong) {
		return elasticSearchBackedResourceDAO.getNewsitemsNearDistanceFacet(latLong, showBrokenDecisionService.shouldShowBroken());
	}
	
	public long getNewsitemsNearCount(LatLong latLong, double radius) {
		return elasticSearchBackedResourceDAO.getGeotaggedNewsitemsNearCount(latLong, radius, showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<Tag> getGeotaggedTags() {
		return elasticSearchBackedResourceDAO.getGeotaggedTags(showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<FrontendResource> getCommentedNewsitems(int maxItems, int startIndex) {
		return elasticSearchBackedResourceDAO.getCommentedNewsitems(maxItems, showBrokenDecisionService.shouldShowBroken(), true, startIndex);
	}

	public int getCommentedNewsitemsCount() {
		return elasticSearchBackedResourceDAO.getCommentedNewsitemsCount(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Tag> getCommentedTags() {
		return elasticSearchBackedResourceDAO.getCommentedTags(showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getLatestNewsitems() {
		return getLatestNewsitems(MAX_NEWSITEMS_TO_SHOW);
	}
	
	public List<FrontendResource> getLatestNewsitems(int maxNumber) {
		return elasticSearchBackedResourceDAO.getLatestNewsitems(maxNumber, showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<FrontendResource> getLatestWebsites(int maxItems) {
		return elasticSearchBackedResourceDAO.getLatestWebsites(maxItems, showBrokenDecisionService.shouldShowBroken());
	}

	public List<TagContentCount> getKeywordSearchFacets(String keywords) {
		return relatedTagsService.getKeywordSearchFacets(keywords, null);	// TODO This is abit odd - it's the only facet one which comes through here.
	}

	public List<FrontendResource> getWebsitesMatchingKeywords(String keywords, Tag tag, int startIndex, int maxItems) {
		return keywordSearchService.getWebsitesMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken(), tag, startIndex, maxItems);
	}
	
	public List<FrontendResource> getNewsitemsMatchingKeywords(String keywords, int startIndex, int maxNewsitems) {
		return keywordSearchService.getNewsitemsMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken(), null, startIndex, maxNewsitems);
	}
	
	public List<FrontendResource> getNewsitemsMatchingKeywords(String keywords, Tag tag, int startIndex, int maxItems) {
		return keywordSearchService.getNewsitemsMatchingKeywords(keywords, showBrokenDecisionService.shouldShowBroken(), tag, startIndex, maxItems);
	}
	
	public int getNewsitemsMatchingKeywordsCount(String keywords, Tag tag) {
		return keywordSearchService.getNewsitemsMatchingKeywordsCount(keywords, showBrokenDecisionService.shouldShowBroken(), tag);
	}

	public int getNewsitemsMatchingKeywordsCount(String keywords) {
		return keywordSearchService.getNewsitemsMatchingKeywordsCount(keywords, showBrokenDecisionService.shouldShowBroken(), null);
	}
	
	public List<FrontendResource> getTwitteredNewsitems(int startIndex, int maxItems) {
		return elasticSearchBackedResourceDAO.getTwitteredNewsitems(startIndex, maxItems, showBrokenDecisionService.shouldShowBroken());
	}
	
	public int getTwitteredNewsitemsCount() {
		return elasticSearchBackedResourceDAO.getTwitteredNewsitemsCount(showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getRecentedTwitteredNewsitemsForTag(int maxItems, Tag tag) {
		return elasticSearchBackedResourceDAO.getRecentTwitteredNewsitemsForTag(maxItems, showBrokenDecisionService.shouldShowBroken(), tag);
	}

	public List<FrontendResource> getFeaturedSites() {
		final Tag featuredTag = tagDAO.loadTagByName("featured");
		if (featuredTag != null) {
			return this.getTaggedWebsites(featuredTag, 10);
		}
		return null;
	}

	public List<FrontendResource> getAllFeeds() {
		return elasticSearchBackedResourceDAO.getAllFeeds(showBrokenDecisionService.shouldShowBroken(), false);
	}
	
	public List<FrontendResource> getAllFeedsOrderByLatestItemDate() {
		return elasticSearchBackedResourceDAO.getAllFeeds(showBrokenDecisionService.shouldShowBroken(), true);
	}
	
	public List<FrontendResource> getPublisherFeeds(Website publisher) {
		return elasticSearchBackedResourceDAO.getPublisherFeeds(publisher, showBrokenDecisionService.shouldShowBroken());
	}

	public long getPublisherNewsitemsCount(Website publisher) {
		return elasticSearchBackedResourceDAO.getPublisherNewsitemsCount(publisher, showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getPublisherNewsitems(Website publisher, int maxItems, int startIndex) {
		return elasticSearchBackedResourceDAO.getPublisherNewsitems(publisher, maxItems, showBrokenDecisionService.shouldShowBroken(), startIndex);
	}

	public List<FrontendResource> getPublisherWatchlist(Website publisher) {
		return elasticSearchBackedResourceDAO.getPublisherWatchlist(publisher, showBrokenDecisionService.shouldShowBroken());
	}

	public List<ArchiveLink> getArchiveMonths() {
		return elasticSearchBackedResourceDAO.getArchiveMonths(showBrokenDecisionService.shouldShowBroken());
	}

	public Map<String, Integer> getArchiveStatistics() {
		return elasticSearchBackedResourceDAO.getArchiveStatistics(showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getCommentedNewsitemsForTag(Tag tag, int maxNewsitems, int startIndex) {
		return elasticSearchBackedResourceDAO.getCommentedNewsitemsForTag(tag, showBrokenDecisionService.shouldShowBroken(), maxNewsitems, startIndex);
	}

	public List<FrontendResource> getNewsitemsForMonth(Date month) {
		return elasticSearchBackedResourceDAO.getNewsitemsForMonth(month, showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendResource> getTaggedWebsites(Set<Tag> tags, int maxItems) {
		return elasticSearchBackedResourceDAO.getTaggedWebsites(tags, showBrokenDecisionService.shouldShowBroken(), maxItems);
	}

	public long getTaggedNewsitemsCount(List<Tag> tags) {
		long taggedNewsitemsCount = elasticSearchBackedResourceDAO.getTaggedNewsitemsCount(tags, showBrokenDecisionService.shouldShowBroken());
		System.out.println(taggedNewsitemsCount);
		return taggedNewsitemsCount;
	}
	
	public List<FrontendResource> getTaggedNewsitems(List<Tag> tags, int startIndex, int maxItems) {
		return elasticSearchBackedResourceDAO.getTaggedNewsitems(tags, showBrokenDecisionService.shouldShowBroken(), startIndex, maxItems);
	}
	
	public List<FrontendResource> getTaggedNewsitems(Tag tag, int startIndex, int maxNewsitems) {
		return elasticSearchBackedResourceDAO.getTaggedNewsitems(tag, showBrokenDecisionService.shouldShowBroken(), startIndex, maxNewsitems);
	}
	
	
	public List<FrontendResource> getTaggedWebsites(Tag tag, int maxItems) {
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		return elasticSearchBackedResourceDAO.getTaggedWebsites(tags, showBrokenDecisionService.shouldShowBroken(), maxItems);
	}

	public List<FrontendResource> getPublisherTagCombinerNewsitems(Website publisher, Tag tag, int maxNewsitems) {
		return elasticSearchBackedResourceDAO.getPublisherTagCombinerNewsitems(publisher, tag, showBrokenDecisionService.shouldShowBroken(), maxNewsitems);
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
		return elasticSearchBackedResourceDAO.getAllWatchlists(showBrokenDecisionService.shouldShowBroken());
	}

	public List<FrontendTag> getFeedworthyTags() {
		final List<FrontendTag> feedworthTags = Lists.newArrayList();
		for (TagContentCount tagContentCount : relatedTagsService.getFeedworthyTags(showBrokenDecisionService.shouldShowBroken())) {
			feedworthTags.add(tagContentCount.getTag());
		}
		return feedworthTags;
	}

	public List<DiscoveredFeed> getDiscoveredFeeds() {
		return discoveredFeedsDAO.getAllNonCommentDiscoveredFeeds();
	}

	public List<Resource> getOwnedBy(User loggedInUser) {
		return resourceDAO.getOwnedBy(loggedInUser, MAX_NEWSITEMS_TO_SHOW);
	}
	
	public List<FrontendResource> getTaggedBy(User user) {
		return elasticSearchBackedResourceDAO.getHandTaggingsForUser(user, showBrokenDecisionService.shouldShowBroken());
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

	public FrontendResource getNewsPage(String pathInfo) {
		return elasticSearchBackedResourceDAO.getNewspage(pathInfo, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Tag> getFeaturedTags() {
		return tagDAO.getFeaturedTags();
	}
	
}
