package nz.co.searchwellington.repositories;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

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

}
