package nz.co.searchwellington.repositories.solr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.decoraters.highlighting.SolrHighlightingNewsitemDecorator;
import nz.co.searchwellington.model.decoraters.highlighting.SolrHighlightingWebsiteDecorator;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class KeywordSearchService {
	
	
	Logger log = Logger.getLogger(KeywordSearchService.class);

	
	private SolrQueryService solrQueryService;
	private SolrKeywordQueryBuilder solrKeywordQueryBuilder;
	private SolrFacetLoader solrFacetLoader;
	private ResourceRepository resourceDAO;
	

	


	public KeywordSearchService(SolrQueryService solrQueryService,
			SolrKeywordQueryBuilder solrKeywordQueryBuilder,
			SolrFacetLoader solrFacetLoader, ResourceRepository resourceDAO) {		
		this.solrQueryService = solrQueryService;
		this.solrKeywordQueryBuilder = solrKeywordQueryBuilder;
		this.solrFacetLoader = solrFacetLoader;
		this.resourceDAO = resourceDAO;
	}


	public List<TagContentCount> getKeywordSearchFacets(String keywords, boolean showBroken, Tag tag) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrKeywordQuery(keywords, showBroken, tag);			
		
		query.setRows(30);
		query.setHighlight(true);
		
		query.addFacetField("publisher");
		query.addFacetField("tags");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetQueryResults = solrQueryService.getFacetQueryResults(query);				
		List<TagContentCount> relatedTagLinks = solrFacetLoader.getRelatedTagLinks(facetQueryResults.get("tags"), null);		
		return relatedTagLinks;		
	}
	
	
	public List<Resource> getNewsitemsMatchingKeywords(String keywords, boolean showBroken, Tag tag) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrNewsitemKeywordQuery(keywords, showBroken, tag);		
		query.setRows(100);
		query.setHighlight(true);
		return getQueryResults(query);
	}
	
	
	
	public List<Resource> getWebsitesMatchingKeywords(String keywords, boolean showBroken, Tag tag) {
		SolrQuery query = solrKeywordQueryBuilder.getSolrWebsiteKeywordQuery(keywords, showBroken, tag);			
		query.setRows(100);
		query.setHighlight(true);		
		return getQueryResults(query);		
	}
	
	
	
	
	private List<Resource> getQueryResults(SolrQuery query) {
		List<Resource> results = new ArrayList<Resource>();		
		QueryResponse response = solrQueryService.querySolr(query);
		if (response != null) {
			loadResourcesFromSolrResults(results, response);			
		}
		return results;
	}
	
		
	private void loadResourcesFromSolrResults(List<Resource> results, QueryResponse response) {
		SolrDocumentList solrResults = response.getResults();
		for (SolrDocument result : solrResults) {
			final Integer resourceId = (Integer) result.getFieldValue("id");
			Resource resource = resourceDAO.loadResourceById(resourceId);			
			if (resource != null) {
				if (response.getHighlighting() != null) {
					Map<String, List<String>> map = response.getHighlighting().get(resourceId.toString());
					if (resource.getType().equals("N") && !map.isEmpty()) {
						results.add(new SolrHighlightingNewsitemDecorator((Newsitem) resource, map));
					} else if (resource.getType().equals("W") && !map.isEmpty()) {
						results.add(new SolrHighlightingWebsiteDecorator((Website) resource, map));
					} else {
						results.add(resource);
					}
				} else {
					results.add(resource);
				}
			} else {
				log.warn("Resource #" + resourceId + " was null onload from database");
			}
		}
	}
		
}
