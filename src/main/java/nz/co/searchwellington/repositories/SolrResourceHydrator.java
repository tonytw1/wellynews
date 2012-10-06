package nz.co.searchwellington.repositories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.frontend.FrontendFeedImpl;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendResourceImpl;
import nz.co.searchwellington.model.frontend.FrontendWebsiteImpl;

import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SolrResourceHydrator implements ResourceHydrator {
	
	private TagDAO tagDAO;	// TODO could remove this by hydrating tag fields from resource
	
	@Autowired
	public SolrResourceHydrator(TagDAO tagDAO) {
		this.tagDAO = tagDAO;
	}
	
	public FrontendResource hydrateResource(SolrDocument result) {
		final int resourceId = Integer.parseInt((String) result.getFieldValue("id"));
		
		FrontendResourceImpl item = null;
		final String type = (String) result.getFieldValue("type");
		if (type.equals("N")) {			
			FrontendNewsitemImpl newsitem = new FrontendNewsitemImpl();
			newsitem.setType("N");
			newsitem.setPublisherName((String) result.getFieldValue(SolrInputDocumentBuilder.PUBLISHER_NAME));
			
			if ((Boolean) result.getFieldValue("commented")) {
				List<Comment> comments = new ArrayList<Comment>();
				Collection<Object> commentFields = result.getFieldValues("comment");
				for (Object commentField : commentFields) {
					comments.add(new Comment((String) commentField));
				}
				newsitem.setComments(comments);
			}
			
			newsitem.setAccepted((Date) result.getFieldValue("accepted"));
			newsitem.setAcceptedFromFeedName((String) result.getFieldValue("acceptedFromFeedName"));
			newsitem.setAcceptedByProfilename((String) result.getFieldValue("acceptedByProfileName"));
			
			hydrateTwitterFields(result, newsitem);
			item = newsitem;			
		}
		
		if (type.equals("W")) {
			FrontendWebsiteImpl publisher = new FrontendWebsiteImpl();			
			publisher.setType("W");
			publisher.setUrlWords((String) result.getFieldValue("urlWords"));
			item = publisher;
		}
		
		if (type.equals("F")) {
			FrontendFeedImpl feed = new FrontendFeedImpl();
			feed.setType("F");
			feed.setPublisherName((String) result.getFieldValue(SolrInputDocumentBuilder.PUBLISHER_NAME));
			feed.setUrlWords((String) result.getFieldValue("urlWords"));
			item = feed;
		}
		
		if (type.equals("L")) {
			item = new FrontendResourceImpl();
			item.setType("L");
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
			item.setOwnerId((Integer) result.getFieldValue("owner"));
			
			if (result.containsKey("geotagged") && (Boolean) result.getFieldValue("geotagged")) {
				Geocode geocode = new Geocode();
				geocode.setAddress((String) result.getFieldValue("address"));
				String positions = (String) result.getFirstValue("position");
				geocode.setLatitude(Double.parseDouble(positions.split(",")[0]));
				geocode.setLongitude(Double.parseDouble(positions.split(",")[1]));
				
				if (result.containsKey("osm_id") && result.getFieldValue("osm_id") != null) {
					final String placeId = (String) result.getFieldValue("osm_id");
					final long osmId = Long.parseLong(placeId.split("/")[0]);
		            final String osmType = placeId.split("/")[1];		            
		            geocode.setOsmId(osmId);
		            geocode.setOsmType(osmType);
				}
								
				item.setGeocode(geocode);
			}
			
			return item;
		}
		
		return null;	
	}

	private void hydrateTwitterFields(SolrDocument result,
			FrontendNewsitemImpl newsitem) {
		final Integer twitterCount = (Integer) result.getFieldValue("twitterCount");
		if (twitterCount != null && twitterCount > 0) {				
			Iterator<Object> twitterAuthors = result.getFieldValues("tweet_author").iterator();
			Iterator<Object> twitterText = result.getFieldValues("tweet_text").iterator();
			List<Twit> twits = new ArrayList<Twit>();
			for (int i = 0; i < twitterCount; i++) {
				Twit tweet = new Twit((String) twitterAuthors.next(), (String) twitterText.next());
				twits.add(tweet);
			}
			newsitem.setRetweets(twits);
		}
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
