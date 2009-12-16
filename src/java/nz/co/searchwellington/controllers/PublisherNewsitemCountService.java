package nz.co.searchwellington.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.SolrQueryBuilder;
import nz.co.searchwellington.repositories.SolrQueryService;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class PublisherNewsitemCountService {
	
	Logger log = Logger.getLogger(PublisherNewsitemCountService.class);

	private LoggedInUserFilter loggedInFilter;
	private SolrQueryService solrQueryService;
	
	Map<Integer, Integer> newsitemCounts;

	
	
	public PublisherNewsitemCountService() {	
	}

		
	public PublisherNewsitemCountService(LoggedInUserFilter loggedInFilter, SolrQueryService solrQueryService) {
		this.loggedInFilter = loggedInFilter;
		this.solrQueryService = solrQueryService;
	}



	public int getNewsitemCount(Website publisher) {	
		boolean showBroken = loggedInFilter.getLoggedInUser() != null;		
		Map<Integer, Integer> newsitemCounts = createOrGetCorrectPublisherNewsitemCounts(showBroken);		
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
		
		List<Count> facetQueryResults = solrQueryService.getFacetQueryResults(query, "publisher");
		if (facetQueryResults != null) {
			Map<Integer, Integer>  newsitemCounts = new HashMap<Integer, Integer>();
			for (Count count : facetQueryResults) {
				final int publisherId = Integer.parseInt(count.getName());						
				final Long relatedItemCount = count.getCount();
				newsitemCounts.put(publisherId, relatedItemCount.intValue());
			}
			return newsitemCounts;
		}
		return null;
    }

}
