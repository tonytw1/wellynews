package nz.co.searchwellington.repositories;

import java.util.Collection;
import java.util.Date;

import nz.co.searchwellington.model.Resource;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;


public class SolrResourceHydrator {

	static Logger log = Logger.getLogger(SolrResourceHydrator.class);
	    
	private ResourceRepository resourceDAO;
	private TagDAO tagDAO;
	
       	
	public SolrResourceHydrator(ResourceRepository resourceDAO, TagDAO tagDAO) {
		this.resourceDAO = resourceDAO;
		this.tagDAO = tagDAO;
	}

	public Resource hydrateResource(SolrDocument result) {
		final Integer resourceId = (Integer) result.getFieldValue("id");				
		if (result.getFieldValue("type").equals("N")) {
			log.info("Solr hydrating");
			Resource item = new SolrHydratedNewsitem(
					resourceId,
					(String) result.getFieldValue("title"), 
					(String) result.getFieldValue("description"),
					(String) result.getFieldValue("url"),
					(String) result.getFieldValue("publisherName"),
					(Date) result.getFieldValue("date")
					);
			
			Collection<Object> tagIds = result.getFieldValues("tags");			
			for (Object tagId : tagIds) {
				log.warn(tagId);
				item.addTag(tagDAO.loadTagById((Integer) tagId));
			}
			return item;
		}
		
		Resource resource = resourceDAO.loadResourceById(resourceId);
		if (resource == null) {
			log.warn("Resource #" + resourceId + " was null onload from database");
		}
		return resource;
		
		
	}

}
