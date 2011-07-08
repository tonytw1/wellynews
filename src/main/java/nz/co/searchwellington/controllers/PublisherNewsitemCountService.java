package nz.co.searchwellington.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.frontend.FrontendWebsite;
import nz.co.searchwellington.repositories.solr.SolrQueryBuilder;
import nz.co.searchwellington.repositories.solr.SolrQueryService;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class PublisherNewsitemCountService {
	
	private LoggedInUserFilter loggedInFilter;
	private SolrQueryService solrQueryService;
	
	private Map<Integer, Integer> newsitemCounts;
	
	public PublisherNewsitemCountService() {	
	}
	
	public PublisherNewsitemCountService(LoggedInUserFilter loggedInFilter, SolrQueryService solrQueryService) {
		this.loggedInFilter = loggedInFilter;
		this.solrQueryService = solrQueryService;
	}
	
	public int getNewsitemCount(FrontendWebsite publisher) {	
		final boolean showBroken = loggedInFilter.getLoggedInUser() != null;		
		Map<Integer, Integer> newsitemCounts = createOrGetCorrectPublisherNewsitemCounts(showBroken);
		if (newsitemCounts == null) {
			return 0;
		}
		final int publisherId = publisher.getId();
		if (newsitemCounts.containsKey(publisherId)) {
			return newsitemCounts.get(publisherId);
		}
		return 0;
	}
	
	private Map<Integer, Integer> createOrGetCorrectPublisherNewsitemCounts(boolean showBroken) {	
		if (this.newsitemCounts == null) {		
			this.newsitemCounts = populatePublisherNewsitemCounts(showBroken);				
		}
		return this.newsitemCounts;
	}
	
	private Map<Integer, Integer> populatePublisherNewsitemCounts(boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(showBroken).toQuery();
		query.addFacetField("publisher");
		query.setFacetMinCount(1);
		query.setFacetLimit(5000);
		
		Map<String, List<Count>> facetQueryResults = solrQueryService.getFacetQueryResults(query);
		List<Count> field = facetQueryResults.get("publisher");
		if (field != null) {
			Map<Integer, Integer>  newsitemCounts = new HashMap<Integer, Integer>();
			for (Count count : field) {
				final int publisherId = Integer.parseInt(count.getName());						
				final Long relatedItemCount = count.getCount();
				newsitemCounts.put(publisherId, relatedItemCount.intValue());
			}
			return newsitemCounts;
		}
		return null;
    }

}
