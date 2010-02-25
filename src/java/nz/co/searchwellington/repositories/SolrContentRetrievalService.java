package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;

import org.apache.ecs.xhtml.map;

import nz.co.searchwellington.controllers.ShowBrokenDecisionService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;

public class SolrContentRetrievalService {

	private SolrBackedResourceDAO solrResourceDAO;
		
	public SolrContentRetrievalService(SolrBackedResourceDAO solrResourceDAO) {
		this.solrResourceDAO = solrResourceDAO;
	}

	public List<Resource> getAllWatchlists(boolean shouldShowBroken) {
		return solrResourceDAO.getAllWatchlists(shouldShowBroken);
	}

	public List<Resource> getTagWatchlist(Tag tag, boolean shouldShowBroken) {
		return solrResourceDAO.getTagWatchlist(tag, shouldShowBroken);
	}

	public List<Resource> getTaggedNewsitems(Tag tag, boolean shouldShowBroken, int startIndex, int maxItems) {
		return solrResourceDAO.getTaggedNewsitems(tag, shouldShowBroken, startIndex, maxItems);
	}

	public List<Resource> getTaggedWebsites(Tag tag, boolean shouldShowBroken, int maxItems) {
		return solrResourceDAO.getTaggedWebsites(tag, shouldShowBroken, maxItems);	
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

}
