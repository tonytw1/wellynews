package nz.co.searchwellington.repositories.solr;

import java.util.List;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.SolrBackedResourceDAO;

import org.apache.solr.client.solrj.SolrQuery;

public class KeywordSearchService {
	
	private SolrKeywordQueryBuilder solrKeywordQueryBuilder;
	private SolrBackedResourceDAO solrBackedResourceDAO;
	
	public KeywordSearchService(
			SolrKeywordQueryBuilder solrKeywordQueryBuilder,
			SolrBackedResourceDAO solrBackedResourceDAO) {
		this.solrKeywordQueryBuilder = solrKeywordQueryBuilder;
		this.solrBackedResourceDAO = solrBackedResourceDAO;
	}

	public List<FrontendResource> getNewsitemsMatchingKeywords(String keywords, boolean showBroken, Tag tag, int startIndex, int maxNewsitems) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrNewsitemKeywordQuery(keywords, showBroken, tag);
		query.setStart(startIndex);
		query.setRows(maxNewsitems);
		query.setHighlight(true);
		return solrBackedResourceDAO.getQueryResults(query);
	}
	
	public int getNewsitemsMatchingKeywordsCount(String keywords, boolean shouldShowBroken, Tag tag) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrNewsitemKeywordQuery(keywords, shouldShowBroken, tag);		
		query.setHighlight(true);
		return solrBackedResourceDAO.getQueryCount(query);
	}
	
	public List<FrontendResource> getWebsitesMatchingKeywords(String keywords, boolean showBroken, Tag tag, int startIndex, int maxItems) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrWebsiteKeywordQuery(keywords, showBroken, tag, startIndex, maxItems);					
		query.setStart(startIndex);
		query.setRows(maxItems);
		query.setHighlight(true);
		return solrBackedResourceDAO.getQueryResults(query);
	}
	
	public List<FrontendResource> getResourcesMatchingKeywords(String keywords, boolean showBroken) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrKeywordQuery(keywords, showBroken, null);			
		query.setRows(100);
		query.setHighlight(true);
		return solrBackedResourceDAO.getQueryResults(query);
	}

	public List<FrontendResource> getResourcesMatchingKeywordsNotTaggedByUser(String keywords, boolean showBroken, User user, Tag tag) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrKeywordQueryNotTaggedByUser(keywords, showBroken, tag, user);			
		query.setRows(100);
		query.setHighlight(true);
		return solrBackedResourceDAO.getQueryResults(query);
	}
	
}
