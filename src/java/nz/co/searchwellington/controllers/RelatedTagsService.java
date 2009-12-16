package nz.co.searchwellington.controllers;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SolrQueryBuilder;
import nz.co.searchwellington.repositories.SolrQueryService;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class RelatedTagsService {
		
    Logger log = Logger.getLogger(RelatedTagsService.class);

	
	private ResourceRepository resourceDAO;
	private SolrQueryService solrQueryService;

	
    
	public RelatedTagsService(ResourceRepository resourceDAO, SolrQueryService solrQueryService) {		
		this.resourceDAO = resourceDAO;
		this.solrQueryService = solrQueryService;
	}
	
	
	public List<TagContentCount> getRelatedTagLinks(Tag tag, boolean showBroken, int maxItems) {    	
		List<TagContentCount> relatedTagLinks = this.getRelatedTagLinks(tag, showBroken);
		if (relatedTagLinks.size() <= maxItems) {
			return relatedTagLinks;
		}
		return relatedTagLinks.subList(0, maxItems);
	}


	public List<TagContentCount> getRelatedTagLinks(Tag tag, boolean showBroken) {    	
    	List<TagContentCount> relatedTags = new ArrayList<TagContentCount>();
	
    	SolrQuery query = new SolrQueryBuilder().tag(tag).showBroken(showBroken).toQuery();
		query.addFacetField("tags");
		query.setFacetMinCount(1);
			
		List<Count> values = solrQueryService.getFacetQueryResults(query, "tags");
		if (values != null) {			
			for (Count count : values) {
				final int relatedTagId = Integer.parseInt(count.getName());
				Tag relatedTag = resourceDAO.loadTagById(relatedTagId);
				if (isTagSuitable(relatedTag, tag)) {
					final Long relatedItemCount = count.getCount();
					relatedTags.add(new TagContentCount(relatedTag, new Integer(relatedItemCount.intValue())));
				}
			}
		}		
		return relatedTags;     
    }
    
    
    
    public List<TagContentCount> getRelatedTagLinks(Website publisher, boolean showBroken) {    	
    	List<TagContentCount> relatedTags = new ArrayList<TagContentCount>();
		
    	SolrQuery query = new SolrQueryBuilder().publisher(publisher).showBroken(showBroken).toQuery();
		query.addFacetField("tags");			
		query.setFacetMinCount(1);
			
		List<Count> values = solrQueryService.getFacetQueryResults(query, "tags");
		if (values != null) {
			for (Count count : values) {
				final int relatedTagId = Integer.parseInt(count.getName());
				Tag relatedTag = resourceDAO.loadTagById(relatedTagId);
				final Long relatedItemCount = count.getCount();
				relatedTags.add(new TagContentCount(relatedTag, relatedItemCount.intValue()));				
			}
		}			
		return relatedTags;     
    }
    
    
    
    
    public List<PublisherContentCount> getRelatedPublisherLinks(Tag tag, boolean showBroken, int maxItems) {    	
		List<PublisherContentCount> relatedLinks = this.getRelatedPublisherLinks(tag, showBroken);
		if (relatedLinks.size() <= maxItems) {
			return relatedLinks;
		}
		return relatedLinks.subList(0, maxItems);
	}
    
    
    public List<PublisherContentCount> getRelatedPublisherLinks(Tag tag, boolean showBroken) {
    	List<PublisherContentCount> relatedPublishers = new ArrayList<PublisherContentCount>();
		
		SolrQuery query = new SolrQueryBuilder().tag(tag).showBroken(showBroken).toQuery();
		query.addFacetField("publisher");			
		query.setFacetMinCount(1);
			
		List<Count> values = solrQueryService.getFacetQueryResults(query, "publisher");
		if (values != null) {			
			for (Count count : values) {
				final int relatedPublisherId = Integer.parseInt(count.getName());
				Website relatedPublisher = (Website) resourceDAO.loadResourceById(relatedPublisherId);				
				final Long relatedItemCount = count.getCount();
				relatedPublishers.add(new PublisherContentCount(relatedPublisher, relatedItemCount.intValue()));					
			}		
		}		
		return relatedPublishers;     
    }
     
	
	private boolean isTagSuitable(Tag relatedTag, Tag tag) {
		return !(tag.equals(relatedTag)) && !(relatedTag.isParentOf(tag) || relatedTag.getAncestors().contains(tag) || relatedTag.isHidden());
	}
	
}
