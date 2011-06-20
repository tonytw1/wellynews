package nz.co.searchwellington.repositories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.FeedImpl;
import nz.co.searchwellington.model.FrontEndNewsitem;
import nz.co.searchwellington.model.FrontEndWebsite;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SolrHydratedNewsitemImpl;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.WebsiteImpl;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;

public class SolrResourceHydrator implements ResourceHydrator {

	private static Logger log = Logger.getLogger(SolrResourceHydrator.class);
	    
	private TagDAO tagDAO;	// TODO could remove this by hydrating tag fields from resource
	
	public SolrResourceHydrator(TagDAO tagDAO) {
		this.tagDAO = tagDAO;
	}
	
	// TODO perf test to see if just loading from db is faster?	
	public Resource hydrateResource(SolrDocument result) {
		final int resourceId = Integer.parseInt((String) result.getFieldValue("id"));
		
		Resource item = null;
		final String type = (String) result.getFieldValue("type");
		if (type.equals("N")) {			
			item = new SolrHydratedNewsitemImpl((String) result.getFieldValue("publisherName"));			
		}
		
		if (type.equals("W")) {
			item = new WebsiteImpl();
		}
		
		if (type.equals("F")) {
			item = new FeedImpl();
		}
		if (type.equals("L")) {
			item = new Watchlist();
		}
		
		if (item != null) {
			
			item.setId(resourceId);
			item.setName((String) result.getFieldValue("title"));
			item.setDescription((String) result.getFieldValue("description"));
			item.setUrl((String) result.getFieldValue("url"));
			item.setDate((Date) result.getFieldValue("date"));
		
			
			if (item.getType().equals("N")) {
				FrontEndNewsitem frontendNewsitem = new FrontEndNewsitem((Newsitem) item);
				frontendNewsitem.setTags(hydrateTags(result, "tags"));
				frontendNewsitem.setHandTags(hydrateTags(result, "handTags"));
				item = frontendNewsitem;
				
			} else if (item.getType().equals("W")) {
				FrontEndWebsite frontendWebsite = new FrontEndWebsite((Website) item);
				frontendWebsite.setTags(hydrateTags(result, "tags"));
				item = frontendWebsite;
			}
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
