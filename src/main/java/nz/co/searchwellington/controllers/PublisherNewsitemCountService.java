package nz.co.searchwellington.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.repositories.SolrInputDocumentBuilder;
import nz.co.searchwellington.repositories.solr.SolrQueryBuilder;
import nz.co.searchwellington.repositories.solr.SolrQueryService;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class PublisherNewsitemCountService {
	
	private LoggedInUserFilter loggedInFilter;
	private SolrQueryService solrQueryService;
	
	private Map<String, Integer> newsitemCounts;
	
	public PublisherNewsitemCountService() {	
	}
	
	public PublisherNewsitemCountService(LoggedInUserFilter loggedInFilter, SolrQueryService solrQueryService) {
		this.loggedInFilter = loggedInFilter;
		this.solrQueryService = solrQueryService;
	}
	
	public int getNewsitemCount(String publisherName) {	
		final boolean showBroken = loggedInFilter.getLoggedInUser() != null;		
		Map<String, Integer> newsitemCounts = createOrGetCorrectPublisherNewsitemCounts(showBroken);
		if (newsitemCounts == null) {
			return 0;
		}
		if (newsitemCounts.containsKey(publisherName)) {
			return newsitemCounts.get(publisherName);
		}
		return 0;
	}
	
	private Map<String, Integer> createOrGetCorrectPublisherNewsitemCounts(boolean showBroken) {	
		if (this.newsitemCounts == null) {		
			this.newsitemCounts = populatePublisherNewsitemCounts(showBroken);				
		}
		return this.newsitemCounts;
	}
	
	private Map<String, Integer> populatePublisherNewsitemCounts(boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(showBroken).toQuery();
		query.addFacetField(SolrInputDocumentBuilder.PUBLISHER_NAME);
		query.setFacetMinCount(1);
		query.setFacetLimit(5000);
		
		Map<String, List<Count>> facetQueryResults = solrQueryService.getFacetQueryResults(query);
		List<Count> field = facetQueryResults.get(SolrInputDocumentBuilder.PUBLISHER_NAME);
		if (field != null) {
			Map<String, Integer>  newsitemCounts = new HashMap<String, Integer>();
			for (Count count : field) {
				final String publisherName = count.getName();
				newsitemCounts.put(publisherName, ((Long) count.getCount()).intValue());
			}
			return newsitemCounts;
		}
		return null;
    }

}
