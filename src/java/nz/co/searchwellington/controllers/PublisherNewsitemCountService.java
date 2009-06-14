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
	
	Map<Integer, Integer> publisherNewsitemCounts;
	
	
	
	public PublisherNewsitemCountService() {	
	}

	
	
	public PublisherNewsitemCountService(LoggedInUserFilter loggedInFilter,
			SolrQueryService solrQueryService) {
		super();
		this.loggedInFilter = loggedInFilter;
		this.solrQueryService = solrQueryService;
	}



	public int getNewsitemCount(Website publisher) {
		int count = publisher.getNewsitems().size();
		boolean showBroken = loggedInFilter.getLoggedInUser() != null;
		if (!showBroken && count > 0) {
			if (publisherNewsitemCounts == null) {
				getPublisherNewsitemCounts(showBroken);
			}
			if (publisherNewsitemCounts.containsKey(publisher.getId())) {
				return publisherNewsitemCounts.get(publisher.getId());
			}
			return 0;
		}
		return count;
	}
	
		
	public void getPublisherNewsitemCounts(boolean showBroken) {
		log.info("Looking up publisher newsitem counts");		
		publisherNewsitemCounts = new HashMap<Integer, Integer>();    	
		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(showBroken).toQuery();
		query.addFacetField("publisher");
		query.setFacetMinCount(1);
		query.setFacetLimit(5000);
		
		List<Count> facetQueryResults = solrQueryService.getFacetQueryResults(query, "publisher");
		if (facetQueryResults != null) {
			for (Count count : facetQueryResults) {
				final int publisherId = Integer.parseInt(count.getName());						
				final Long relatedItemCount = count.getCount();
				publisherNewsitemCounts.put(publisherId, relatedItemCount.intValue());
			}
		}
    }

}
