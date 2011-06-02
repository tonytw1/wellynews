package nz.co.searchwellington.repositories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.FrontEndNewsitem;
import nz.co.searchwellington.model.FrontEndWebsite;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SolrHydratedNewsitemImpl;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;

public class SolrResourceHydrator {

	private static Logger log = Logger.getLogger(SolrResourceHydrator.class);
	    
	private ResourceRepository resourceDAO;
	private TagDAO tagDAO;	// TODO could remove this by hydrating tag fields from resource
	
	public SolrResourceHydrator(ResourceRepository resourceDAO, TagDAO tagDAO) {
		this.resourceDAO = resourceDAO;
		this.tagDAO = tagDAO;
	}
	
	public Resource hydrateResource(SolrDocument result) {
		final int resourceId = Integer.parseInt((String) result.getFieldValue("id"));
		
		boolean solrHydrate = false;
		if (result.getFieldValue("type").equals("N") && solrHydrate) {
			log.debug("Solr hydrating newsitem");
			
			Resource item = new SolrHydratedNewsitemImpl((String) result.getFieldValue("publisherName"));
			item.setName((String) result.getFieldValue("title"));
			item.setDescription((String) result.getFieldValue("description"));
			item.setUrl((String) result.getFieldValue("url"));
			item.setDate((Date) result.getFieldValue("date"));
			return item;
		}
		
		Resource resource = resourceDAO.loadResourceById(resourceId);
		if (resource == null) {
			log.warn("Resource #" + resourceId + " was null onload from database");
			return null;
		}
		
		if (resource.getType().equals("N")) {
			FrontEndNewsitem frontendNewsitem = new FrontEndNewsitem((Newsitem) resource);
			frontendNewsitem.setTags(hydrateTags(result, "tags"));
			frontendNewsitem.setHandTags(hydrateTags(result, "handTags"));
			resource = frontendNewsitem;
			
		} else if (resource.getType().equals("W")) {
			FrontEndWebsite frontendWebsite = new FrontEndWebsite((Website) resource);
			frontendWebsite.setTags(hydrateTags(result, "tags"));
			resource = frontendWebsite;
		}
		
		return resource;
	}
	
	private List<Tag> hydrateTags(SolrDocument result, String sourceField) {
		List<Tag> tags = new ArrayList<Tag>();
		Collection<Object> tagIds = result.getFieldValues(sourceField);
		if (tagIds != null) {
			for (Object tagId : tagIds) {
				Tag tag = tagDAO.loadTagById((Integer) tagId);
				if (tag != null && !tag.isHidden()) {					
					tags.add(tag);
				}
			}
		}
		return tags;
	}

}
