package nz.co.searchwellington.repositories.solr;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.solr.client.solrj.response.FacetField.Count;

public class SolrFacetLoader {
	
	private TagDAO tagDAO;
	
	public SolrFacetLoader(TagDAO tagDAO) {		
		this.tagDAO = tagDAO;
	}
	
	public List<TagContentCount> loadTagFacet(List<Count> values) {
    	List<TagContentCount> relatedTags = new ArrayList<TagContentCount>();
    	if (values != null) {
    		for (Count count : values) {
    			final int relatedTagId = Integer.parseInt(count.getName());
    			Tag relatedTag = tagDAO.loadTagById(relatedTagId);
    			if (relatedTag != null) {
    				final Long relatedItemCount = count.getCount();
    				relatedTags.add(new TagContentCount(relatedTag, new Integer(relatedItemCount.intValue())));
    			}
			}
		}
		return relatedTags;
    }
	
    public List<PublisherContentCount> loadPublisherFacet(List<Count> values) {
    	List<PublisherContentCount> relatedPublishers = new ArrayList<PublisherContentCount>();		
		if (values != null) {
			for (Count count : values) {
				final String relatedPublisherName = (String) count.getName();
				final Long relatedItemCount = count.getCount();
				relatedPublishers.add(new PublisherContentCount(relatedPublisherName, relatedItemCount.intValue()));
			}
		}		
		return relatedPublishers;     
    }
    
}
