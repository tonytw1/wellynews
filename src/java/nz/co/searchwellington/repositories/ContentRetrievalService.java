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
import nz.co.searchwellington.repositories.solr.KeywordSearchService;

import org.apache.log4j.Logger;

public class ContentRetrievalService {

	static Logger log = Logger.getLogger(ContentRetrievalService.class);
	
    private static final int HOW_FAR_IS_CLOSE_IN_KILOMETERS = 1;
	final protected int MAX_NEWSITEMS_TO_SHOW = 30;
        
	private ResourceRepository resourceDAO;
	private SolrContentRetrievalService solrContentRetrievalService;
	private KeywordSearchService keywordSearchService;
	private ShowBrokenDecisionService showBrokenDecisionService;
	private TagDAO tagDAO;
	private RelatedTagsService relatedTagsService;
	private DiscoveredFeedRepository discoveredFeedsDAO;
		

	public ContentRetrievalService(ResourceRepository resourceDAO,
			SolrContentRetrievalService solrContentRetrievalService,
			KeywordSearchService keywordSearchService,
			ShowBrokenDecisionService showBrokenDecisionService, TagDAO tagDAO,
			RelatedTagsService relatedTagsService,
			DiscoveredFeedRepository discoveredFeedsDAO) {
		this.resourceDAO = resourceDAO;
		this.solrContentRetrievalService = solrContentRetrievalService;
		this.keywordSearchService = keywordSearchService;
		this.showBrokenDecisionService = showBrokenDecisionService;
		this.tagDAO = tagDAO;
		this.relatedTagsService = relatedTagsService;
		this.discoveredFeedsDAO = discoveredFeedsDAO;
	}

	
	public List<Resource> getAllWatchlists() {
		return solrContentRetrievalService.getAllWatchlists(showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getGeocoded(int startIndex, int maxItems) {		
		return solrContentRetrievalService.getValidGeotagged(startIndex, maxItems, showBrokenDecisionService.shouldShowBroken());
	}
	
	public int getGeotaggedCount() {
		return solrContentRetrievalService.getGeotaggedCount(showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<Resource> getAllPublishers() {
		return solrContentRetrievalService.getAllPublishers(showBrokenDecisionService.shouldShowBroken());
	}
	
	public List<Tag> getTopLevelTags() {
		return tagDAO.getTopLevelTags();
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
		return solrContentRetrievalService.getLastLiveTimeForTag(tag);
	}

	public List<Resource> getTaggedFeeds(Tag tag) {
		return solrContentRetrievalService.getTaggedFeeds(tag, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getTaggedGeotaggedNewsitems(Tag tag, int maxItems) {
		return solrContentRetrievalService.getTaggedGeotaggedNewsitems(tag, maxItems, showBrokenDecisionService.shouldShowBroken());
	}
	
	// TODO You might get away with this for a little while, but it needs to go into solr if at all possible - full set iteration is not good
	public List<Resource> getGeotaggedNewsitemsNear(double latitude, double longitude) {
		log.info("Querying for geotagged newsitems within " + HOW_FAR_IS_CLOSE_IN_KILOMETERS + " kilometers of: " + latitude + ", " + longitude);
		List<Resource> nearByNewsitems = new ArrayList<Resource>();
		for (Resource newsitem : getGeocoded(0, 500)) {			
			newsitem = resourceDAO.loadResourceById(newsitem.getId());
			if (newsitem != null && newsitem.getGeocode() != null && newsitem.getGeocode().isValid()) {
				if (newsitem.getGeocode().getDistanceTo(latitude, longitude) < HOW_FAR_IS_CLOSE_IN_KILOMETERS) {
					nearByNewsitems.add(newsitem);
				}				
			} else {
				log.debug(newsitem.getName() + " has invalid geocode: " + newsitem.getGeocode());
			}
		}
		return nearByNewsitems;		
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
	
	public List<Resource> getRecentedTwitteredNewsitems() {
		return solrContentRetrievalService.getRecentTwitteredNewsitems(MAX_NEWSITEMS_TO_SHOW, showBrokenDecisionService.shouldShowBroken());
	}

	public List<Resource> getRecentedTwitteredNewsitemsForTag(int maxItems, Tag tag) {
		return solrContentRetrievalService.getRecentTwitteredNewsitemsForTag(maxItems, showBrokenDecisionService.shouldShowBroken(), tag);
	}

	public List<Resource> getFeaturedSites() {
		final Tag featuredTag = tagDAO.loadTagByName("featured");
		if (featuredTag != null) {
			return this.getTaggedWebsites(featuredTag, 10);
		}
		return null;
	}

	public List<Resource> getAllFeeds() {
		return solrContentRetrievalService.getAllFeeds(showBrokenDecisionService.shouldShowBroken());
	}
	
	public Feed getFeedByUrlWord(String feedUrlWords) {
		return resourceDAO.loadFeedByUrlWords(feedUrlWords);
	}
	
	public List<Resource> getAllFeedsOrderByLatestItemDate() {
		return solrContentRetrievalService.getAllFeedsOrderByLatestItemDate(showBrokenDecisionService.shouldShowBroken());
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

	public List<Resource> getBrokenSites() {
		return solrContentRetrievalService.getBrokenSites();	
	}

	public List<Resource> getPublisherTagCombinerNewsitems(Website publisher, Tag tag, int maxNewsitems) {
		return solrContentRetrievalService.getPublisherTagCombinerNewsitems(publisher, tag, showBrokenDecisionService.shouldShowBroken(), maxNewsitems);
	}
	
	public List<Resource> getPublisherTagCombinerNewsitems(String publisherUrlWords, String tagName, int maxNewsitems) {
		Website publisher = resourceDAO.getPublisherByUrlWords(publisherUrlWords);
		Tag tag = tagDAO.loadTagByName(tagName);
		if (publisher != null && tag != null) {
			return this.getPublisherTagCombinerNewsitems(publisher, tag, maxNewsitems);
		}
		return null;		
	}
	
	public List<Resource> getRecentlyChangedWatchlistItems() {
		return solrContentRetrievalService.getRecentlyChangedWatchlistItems(showBrokenDecisionService.shouldShowBroken());
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
	
	public List<Resource> getTaggedBy(User user, int maxItems) {
		return solrContentRetrievalService.getHandTaggingsForUser(user, showBrokenDecisionService.shouldShowBroken());
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
		return solrContentRetrievalService.getNewsPage(pathInfo, showBrokenDecisionService.shouldShowBroken());
	}
	
}
