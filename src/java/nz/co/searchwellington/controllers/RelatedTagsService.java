package nz.co.searchwellington.controllers;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class RelatedTagsService {
		
	private static final int MAX_WEBSITES = 100;
	
	
    Logger log = Logger.getLogger(RelatedTagsService.class);

	
	private ResourceRepository resourceDAO;

	
	public RelatedTagsService(ResourceRepository resourceDAO) {
		this.resourceDAO = resourceDAO;
	}


    public List<TagContentCount> getRelatedTagLinks(Tag tag, boolean showBroken) {    	
    	return getRelatedTags(tag, true);     
    }


	private List<TagContentCount> getRelatedTags(Tag tag, boolean b) {
		List<TagContentCount> relatedTags = new ArrayList<TagContentCount>();
		try {
			SolrServer solr = new CommonsHttpSolrServer("http://localhost:8080/apache-solr-1.3.0");
		
			SolrQuery query = new SolrQuery("tags:" + tag.getId());
			query.addFacetField("tags");			
			query.setFacetLimit(20);
			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return relatedTags;
	}
	
}
