package nz.co.searchwellington.repositories.solr;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.solr.client.solrj.response.FacetField.Count;

public class SolrFacetLoader {

	
	private ResourceRepository resourceDAO;

	
	public SolrFacetLoader(ResourceRepository resourceDAO) {		
		this.resourceDAO = resourceDAO;
	}


	public List<TagContentCount> loadTagFacet(List<Count> values) {
    	List<TagContentCount> relatedTags = new ArrayList<TagContentCount>();
    	if (values != null) {
    		for (Count count : values) {
    			final int relatedTagId = Integer.parseInt(count.getName());
    			Tag relatedTag = resourceDAO.loadTagById(relatedTagId);    			
				final Long relatedItemCount = count.getCount();
				relatedTags.add(new TagContentCount(relatedTag, new Integer(relatedItemCount.intValue())));				
			}
		}
		return relatedTags;
    }
    
	
    public List<PublisherContentCount> loadPublisherFacet(List<Count> values) {
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
    
}
