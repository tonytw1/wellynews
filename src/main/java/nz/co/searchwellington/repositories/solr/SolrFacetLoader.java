package nz.co.searchwellington.repositories.solr;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendWebsiteImpl;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.solr.client.solrj.response.FacetField.Count;

public class SolrFacetLoader {

	private ResourceRepository resourceDAO;	// TODO Try to drive out
	private TagDAO tagDAO;
	
	public SolrFacetLoader(ResourceRepository resourceDAO, TagDAO tagDAO) {		
		this.resourceDAO = resourceDAO;
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
				final int relatedPublisherId = Integer.parseInt(count.getName());
				Website relatedPublisher = (Website) resourceDAO.loadResourceById(relatedPublisherId);		// TODO Try to drive out			
				if (relatedPublisher != null) {
					final Long relatedItemCount = count.getCount();
				
					FrontendWebsiteImpl frontendWebsite = new FrontendWebsiteImpl();	// TODO Hack - need to tighten up on what information really needs to be in a publisher count
					frontendWebsite.setName(relatedPublisher.getName());
					frontendWebsite.setUrlWords(relatedPublisher.getUrlWords());
				
					relatedPublishers.add(new PublisherContentCount(frontendWebsite, relatedItemCount.intValue()));
				}
			}
		}	
		return relatedPublishers;     
    }
    
}
