package nz.co.searchwellington.repositories;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SolrHydratedNewsitemImpl;
import nz.co.searchwellington.model.Tag;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;


public class SolrResourceHydrator {

	static Logger log = Logger.getLogger(SolrResourceHydrator.class);
	    
	private ResourceRepository resourceDAO;
	private TagDAO tagDAO;	// TODO could remove this by hydrating tag fields from resource
	       	
	public SolrResourceHydrator(ResourceRepository resourceDAO, TagDAO tagDAO) {
		this.resourceDAO = resourceDAO;
		this.tagDAO = tagDAO;
	}

	public Resource hydrateResource(SolrDocument result) {
		final Integer resourceId = (Integer) result.getFieldValue("id");				
		if (result.getFieldValue("type").equals("N")) {
			log.info("Solr hydrating newsitem");
			
			Resource item = new SolrHydratedNewsitemImpl((String) result.getFieldValue("publisherName"));
			item.setName((String) result.getFieldValue("title"));
			item.setDescription((String) result.getFieldValue("description"));
			item.setUrl((String) result.getFieldValue("url"));
			item.setDate((Date) result.getFieldValue("date"));
			item.setTags(new HashSet<Tag>());
			hydrateTags(result, item);
			return item;
		}
		
		Resource resource = resourceDAO.loadResourceById(resourceId);
		if (resource == null) {
			log.warn("Resource #" + resourceId + " was null onload from database");
		}
		return resource;		
	}

	private void hydrateTags(SolrDocument result, Resource item) {
		Collection<Object> tagIds = result.getFieldValues("tags");
		if (tagIds != null){
			for (Object tagId : tagIds) {
				Tag tag = tagDAO.loadTagById((Integer) tagId);
				if (tag != null) {
					log.info(tag.getName());
					item.addTag(tag);
				}
			}
		}
	}

}
