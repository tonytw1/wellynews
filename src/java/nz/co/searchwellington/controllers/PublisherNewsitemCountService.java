package nz.co.searchwellington.controllers;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.SolrQueryBuilder;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class PublisherNewsitemCountService {
	
	Logger log = Logger.getLogger(PublisherNewsitemCountService.class);

	private LoggedInUserFilter loggedInFilter;
	private String solrUrl;
	
	Map<Integer, Integer> publisherNewsitemCounts;
	
	
	
	public PublisherNewsitemCountService() {	
	}


	public PublisherNewsitemCountService(LoggedInUserFilter loggedInFilter) {
		this.loggedInFilter = loggedInFilter;
	}
	
	
	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
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
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);		
			SolrQuery query = new SolrQueryBuilder().type("N").showBroken(showBroken).toQuery();
			query.addFacetField("publisher");
			query.setFacetMinCount(1);
			
			QueryResponse response = solr.query(query);							
			FacetField facetField = response.getFacetField("publisher");
			if (facetField != null && facetField.getValues() != null) {
				log.info("Found facet field: " + facetField);
				List<Count> values = facetField.getValues();
				for (Count count : values) {
					final int publisherId = Integer.parseInt(count.getName());						
					final Long relatedItemCount = count.getCount();
					publisherNewsitemCounts.put(publisherId, relatedItemCount.intValue());
				}		
			}
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}					
    }
}
