package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;

public class SolrContentRetrievalService {

	private SolrBackedResourceDAO solrResourceDAO;
		
	public SolrContentRetrievalService(SolrBackedResourceDAO solrResourceDAO) {
		this.solrResourceDAO = solrResourceDAO;
	}
	
	public List<Resource> getAllFeeds(boolean shouldShowBroken) {
		return solrResourceDAO.getAllFeeds(shouldShowBroken);
	}

	public List<Resource> getAllWatchlists(boolean shouldShowBroken) {
		return solrResourceDAO.getAllWatchlists(shouldShowBroken);
	}

	public List<Resource> getTagWatchlist(Tag tag, boolean shouldShowBroken) {
		return solrResourceDAO.getTagWatchlist(tag, shouldShowBroken);
	}

	public List<Resource> getTaggedWebsites(Set<Tag> tags, boolean shouldShowBroken, int maxItems) {
		return solrResourceDAO.getTaggedWebsites(tags, shouldShowBroken, maxItems);	
	}

	public List<Resource> getRecentCommentedNewsitemsForTag(Tag tag, boolean shouldShowBroken, int maxItems) {
		return solrResourceDAO.getRecentCommentedNewsitemsForTag(tag, shouldShowBroken, maxItems);
	}

	public int getCommentedNewsitemsForTagCount(Tag tag, boolean shouldShowBroken) {
		return solrResourceDAO.getCommentedNewsitemsForTagCount(tag, shouldShowBroken);
	}

	public int getTaggedNewitemsCount(Tag tag, boolean shouldShowBroken) {
		return solrResourceDAO.getTaggedNewitemsCount(tag, shouldShowBroken);
	}

	public List<Resource> getTaggedFeeds(Tag tag, boolean shouldShowBroken) {
		return solrResourceDAO.getTaggedFeeds(tag, shouldShowBroken);
	}

	public int getCommentedNewsitemsCount(boolean shouldShowBroken) {
		return solrResourceDAO.getCommentedNewsitemsCount(shouldShowBroken);
	}
	
	public List<Resource> getPublisherNewsitems(Website publisher, int maxItems, boolean shouldShowBroken, int startIndex) {
		return solrResourceDAO.getPublisherNewsitems(publisher, maxItems, shouldShowBroken, startIndex);
	}
	
	public int getPublisherNewsitemsCount(Website publisher, boolean shouldShowBroken) {
		return solrResourceDAO.getPublisherNewsitemsCount(publisher, shouldShowBroken);
	}

	public List<Resource> getNewsitemsForMonth(Date month, boolean shouldShowBroken) {
		return solrResourceDAO.getNewsitemsForMonth(month, shouldShowBroken);
	}

	public List<Resource> getCommentedNewsitemsForTag(Tag tag, boolean shouldShowBroken, int maxNewsitems, int startIndex) {
		return solrResourceDAO.getCommentedNewsitemsForTag(tag, shouldShowBroken, maxNewsitems, startIndex);
	}

	public List<Resource> getLatestWebsites(int maxItems, boolean shouldShowBroken) {
	    return solrResourceDAO.getLatestWebsites(maxItems, shouldShowBroken);
	}

	public List<Resource> getLatestNewsitems(int maxItems, boolean shouldShowBroken) {
		return solrResourceDAO.getLatestNewsitems(maxItems, shouldShowBroken);
	}

	public List<Resource> getPublisherFeeds(Website publisher, boolean shouldShowBroken) {
		return solrResourceDAO.getPublisherFeeds(publisher, shouldShowBroken);
	}

	public List<Resource> getPublisherWatchlist(Website publisher, boolean shouldShowBroken) {
		return solrResourceDAO.getPublisherWatchlist(publisher, shouldShowBroken);
	}

	public List<PublisherContentCount> getAllPublishersWithNewsitemCounts(boolean shouldShowBroken, boolean mustHaveNewsitems) {
		return solrResourceDAO.getAllPublishersWithNewsitemCounts(shouldShowBroken, mustHaveNewsitems);
	}

	public List<Resource> getAllPublishers(boolean shouldShowBroken, boolean mustHaveNewsitems) {
		return solrResourceDAO.getAllPublishers(shouldShowBroken, mustHaveNewsitems);
	}

	public List<Resource> getTaggedNewsitems(Set<Tag> tags, boolean shouldShowBroken, int startIndex, int maxItems) {
		return solrResourceDAO.getTaggedNewsitems(tags, shouldShowBroken, startIndex, maxItems);
		
	}

	public List<Tag> getGeotaggedTags(boolean shouldShowBroken) {
		return solrResourceDAO.getGeotaggedTags(shouldShowBroken);
	}

	public List<Tag> getCommentedTags(boolean shouldShowBroken) {
		return solrResourceDAO.getCommentedTags(shouldShowBroken);
	}

	public List<ArchiveLink> getArchiveMonths(boolean shouldShowBroken) {
		return solrResourceDAO.getArchiveMonths(shouldShowBroken);
	}

	public List<Resource> getCommentedNewsitems(int maxItems, boolean shouldShowBroken, boolean hasComments, int startIndex) {
		return solrResourceDAO.getCommentedNewsitems(maxItems, shouldShowBroken, hasComments, startIndex);
	}

	public Map<String, Integer> getArchiveStatistics(boolean shouldShowBroken) {
		return solrResourceDAO.getArchiveStatistics(shouldShowBroken);
	}

	public List<Resource> getRecentTwitteredNewsitems(int maxItems, boolean shouldShowBroken) {
		return solrResourceDAO.getRecentTwitteredNewsitems(maxItems, shouldShowBroken);
	}

	public List<Resource> getRecentTwitteredNewsitemsForTag(int maxItems, boolean shouldShowBroken, Tag tag) {
		return solrResourceDAO.getRecentTwitteredNewsitemsForTag(maxItems, shouldShowBroken, tag);
	};
	
}
