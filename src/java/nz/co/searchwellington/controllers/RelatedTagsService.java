package nz.co.searchwellington.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
		
	public TagRelatedLinks getTagsRelatedLinks(Tag tag, boolean showBroken, int maxItems) {
		TagRelatedLinks relatedLinks = new TagRelatedLinks();
	
		SolrQuery query = new SolrQueryBuilder().tag(tag).showBroken(showBroken).toQuery();
		query.addFacetField("tags");
		query.addFacetField("publisher");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetResuls = solrQueryService.getFacetQueryResults(query);		
		relatedLinks.setRelatedTags(getRelatedTagLinks(facetResuls.get("tags"), tag));
		relatedLinks.setRelatedPublisers(getRelatedPublisherLinks(facetResuls.get("publisher")));
		return relatedLinks;
	}
	
	

	public List<TagContentCount> getRelatedTagLinks(Website publisher, boolean showBroken) {
		SolrQuery query = new SolrQueryBuilder().publisher(publisher).showBroken(showBroken).toQuery();
		query.addFacetField("tags");
		query.setFacetMinCount(1);
		
		Map<String, List<Count>> facetResuls = solrQueryService.getFacetQueryResults(query);
		return getRelatedTagLinks(facetResuls.get("tags"), null);
	}
	
	
	private List<TagContentCount> getRelatedTagLinks(List<Count> values, Tag ignoreTag) {
    	List<TagContentCount> relatedTags = new ArrayList<TagContentCount>();
    	if (values != null) {
    		for (Count count : values) {
    			final int relatedTagId = Integer.parseInt(count.getName());
    			Tag relatedTag = resourceDAO.loadTagById(relatedTagId);
    			if (isTagSuitable(relatedTag, ignoreTag)) {
					final Long relatedItemCount = count.getCount();
					relatedTags.add(new TagContentCount(relatedTag, new Integer(relatedItemCount.intValue())));
				}
			}
		}
		return relatedTags;     
    }
    
	
    private List<PublisherContentCount> getRelatedPublisherLinks(List<Count> values) {
    	List<PublisherContentCount> relatedPublishers = new ArrayList<PublisherContentCount>();		
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
     
	
	private boolean isTagSuitable(Tag relatedTag, Tag ignoreTag) {
		if (ignoreTag != null) {
			return !(ignoreTag.equals(relatedTag)) && !(relatedTag.isParentOf(ignoreTag) || relatedTag.getAncestors().contains(ignoreTag) || relatedTag.isHidden());
		}
		return true;
	}


	
}
