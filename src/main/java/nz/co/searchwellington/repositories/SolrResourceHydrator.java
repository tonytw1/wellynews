package nz.co.searchwellington.repositories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendResourceImpl;

import org.apache.solr.common.SolrDocument;

public class SolrResourceHydrator implements ResourceHydrator {
	
	private TagDAO tagDAO;	// TODO could remove this by hydrating tag fields from resource
	
	public SolrResourceHydrator(TagDAO tagDAO) {
		this.tagDAO = tagDAO;
	}
	
	public FrontendResource hydrateResource(SolrDocument result) {
		final int resourceId = Integer.parseInt((String) result.getFieldValue("id"));
		
		FrontendResourceImpl item = null;
		final String type = (String) result.getFieldValue("type");
		if (type.equals("N")) {			
			FrontendNewsitemImpl newsitem = new FrontendNewsitemImpl();
			newsitem.setPublisherName((String) result.getFieldValue("publisherName"));			
			if ((Boolean) result.getFirstValue("geotagged")) {
				Geocode geocode = new Geocode();			
				geocode.setAddress((String) result.getFieldValue("address"));
				newsitem.setGeocode(geocode);
			}			
			item = newsitem;			
		}
		
		if (type.equals("W")) {
			item = new FrontendResourceImpl();
		}
		
		if (type.equals("F")) {
			item = new FrontendResourceImpl();
		}
		if (type.equals("L")) {
			item = new FrontendResourceImpl();
		}
		
		if (item != null) {			
			item.setId(resourceId);
			item.setName((String) result.getFieldValue("title"));
			item.setDescription((String) result.getFieldValue("description"));
			item.setUrl((String) result.getFieldValue("url"));
			item.setHttpStatus((int) ((Integer) result.getFieldValue("httpStatus")));
			item.setDate((Date) result.getFieldValue("date"));
			item.setTags(hydrateTags(result, "tags"));
			item.setHandTags(hydrateTags(result, "handTags"));
			return item;
		}
		
		return null;	
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
