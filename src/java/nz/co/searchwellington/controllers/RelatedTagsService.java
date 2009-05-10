package nz.co.searchwellington.controllers;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class RelatedTagsService {
		
    Logger log = Logger.getLogger(RelatedTagsService.class);

	
	private ResourceRepository resourceDAO;
	// TODO Spring prop
	String solrUrl = "http://localhost:8080/apache-solr-1.3.0";

	
	public RelatedTagsService(ResourceRepository resourceDAO) {
		this.resourceDAO = resourceDAO;
	}


    public List<TagContentCount> getRelatedTagLinks(Tag tag, boolean showBroken) {    	
    	List<TagContentCount> relatedTags = new ArrayList<TagContentCount>();
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);		
			SolrQuery query = getTagSolrQuery(tag);
			query.addFacetField("tags");			
			query.setFacetLimit(10);
			query.setFacetMinCount(1);
			
			QueryResponse response = solr.query(query);		
					
			FacetField facetField = response.getFacetField("tags");
			log.info("Found facet field: " + facetField);
			List<Count> values = facetField.getValues();
			for (Count count : values) {
				final int relatedTagId = Integer.parseInt(count.getName());
				Tag relatedTag = resourceDAO.loadTagById(relatedTagId);
				if (isTagSuitable(relatedTag, tag)) {
					final long relatedItemCount = count.getCount();
					relatedTags.add(new TagContentCount(relatedTag, relatedItemCount));					
				}
			}		
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}
		return relatedTags;     
    }
    
    
    
    public List<TagContentCount> getRelatedTagLinks(Website publisher, boolean showBroken) {    	
    	List<TagContentCount> relatedTags = new ArrayList<TagContentCount>();
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);		
			SolrQuery query = new SolrQuery("publisher:" + publisher.getId());
			query.addFacetField("tags");
			query.setFacetLimit(10);
			query.setFacetMinCount(1);
			
			QueryResponse response = solr.query(query);		
					
			FacetField facetField = response.getFacetField("tags");
			log.info("Found facet field: " + facetField);
			List<Count> values = facetField.getValues();
			for (Count count : values) {
				final int relatedTagId = Integer.parseInt(count.getName());
				Tag relatedTag = resourceDAO.loadTagById(relatedTagId);
				final long relatedItemCount = count.getCount();
				relatedTags.add(new TagContentCount(relatedTag, relatedItemCount));				
			}		
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}
		return relatedTags;     
    }
    
    

	private SolrQuery getTagSolrQuery(Tag tag) {
		SolrQuery query = new SolrQuery("tags:" + tag.getId());
		return query;
	}

	
    public List<PublisherContentCount> getRelatedPublisherLinks(Tag tag, boolean showBroken) {
    	List<PublisherContentCount> relatedPublishers = new ArrayList<PublisherContentCount>();
		try {
			SolrServer solr = new CommonsHttpSolrServer(solrUrl);		
			SolrQuery query = getTagSolrQuery(tag);
			query.addFacetField("publisher");	
			query.setFacetLimit(10);
			query.setFacetMinCount(1);
			
			QueryResponse response = solr.query(query);		
					
			FacetField facetField = response.getFacetField("publisher");
			log.info("Found facet field: " + facetField);
			List<Count> values = facetField.getValues();
			for (Count count : values) {
				final int relatedPublisherId = Integer.parseInt(count.getName());
				Website relatedPublisher = (Website) resourceDAO.loadResourceById(relatedPublisherId);
				
				final long relatedItemCount = count.getCount();
				relatedPublishers.add(new PublisherContentCount(relatedPublisher, relatedItemCount));
				
			}		
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (SolrServerException e) {
			log.error(e);
		}
		return relatedPublishers;     
    }
     
	
	private boolean isTagSuitable(Tag relatedTag, Tag tag) {
		return !(tag.equals(relatedTag)) && !(relatedTag.isParentOf(tag) || relatedTag.getAncestors().contains(tag));
	}
	
}
