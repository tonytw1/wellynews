package nz.co.searchwellington.repositories.solr;

import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.SolrBackedResourceDAO;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class KeywordSearchService {
		
	private static final int SEARCH_FACETS_TO_SHOW = 30;
	
	private SolrQueryService solrQueryService;
	private SolrKeywordQueryBuilder solrKeywordQueryBuilder;
	private SolrFacetLoader solrFacetLoader;
	private SolrBackedResourceDAO solrBackedResourceDAO;
	
	
	public KeywordSearchService(SolrQueryService solrQueryService,
			SolrKeywordQueryBuilder solrKeywordQueryBuilder,
			SolrFacetLoader solrFacetLoader,
			SolrBackedResourceDAO solrBackedResourceDAO) {
		super();
		this.solrQueryService = solrQueryService;
		this.solrKeywordQueryBuilder = solrKeywordQueryBuilder;
		this.solrFacetLoader = solrFacetLoader;
		this.solrBackedResourceDAO = solrBackedResourceDAO;
	}


	public List<TagContentCount> getKeywordSearchFacets(String keywords, boolean showBroken, Tag tag) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrKeywordQuery(keywords, showBroken, tag);			
		
		query.setRows(SEARCH_FACETS_TO_SHOW);
		query.setHighlight(true);
		
		query.addFacetField("publisher");
		query.addFacetField("tags");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetQueryResults = solrQueryService.getFacetQueryResults(query);				
		List<TagContentCount> relatedTagLinks = solrFacetLoader.loadTagFacet(facetQueryResults.get("tags"));		
		return relatedTagLinks;		
	}
	
	public List<FrontendResource> getNewsitemsMatchingKeywords(String keywords, boolean showBroken, Tag tag) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrNewsitemKeywordQuery(keywords, showBroken, tag);		
		query.setRows(100);
		query.setHighlight(true);
		return solrBackedResourceDAO.getQueryResults(query);
	}
		
	public List<FrontendResource> getWebsitesMatchingKeywords(String keywords, boolean showBroken, Tag tag) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrWebsiteKeywordQuery(keywords, showBroken, tag);			
		query.setRows(100);
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
