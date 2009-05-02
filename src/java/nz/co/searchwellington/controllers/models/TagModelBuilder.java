 package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ConfigDAO;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.utils.UrlFilters;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class TagModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	Logger log = Logger.getLogger(TagModelBuilder.class);
    	
	private ResourceRepository resourceDAO;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private RelatedTagsService relatedTagsService;
	private ConfigDAO configDAO;
	private RssfeedNewsitemService rssfeedNewsitemService;
	
	 
	public TagModelBuilder(ResourceRepository resourceDAO, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder, RelatedTagsService relatedTagsService, ConfigDAO configDAO, RssfeedNewsitemService rssfeedNewsitemService) {
		this.resourceDAO = resourceDAO;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.relatedTagsService = relatedTagsService;
		this.configDAO = configDAO;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
	}

	
	@SuppressWarnings("unchecked")
	public boolean isValid(HttpServletRequest request) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		boolean isSingleTagPage = tags != null && tags.size() == 1;
		return isSingleTagPage;
	}

	@SuppressWarnings("unchecked")
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building tag page model");
			List<Tag> tags = (List<Tag>) request.getAttribute("tags");
			Tag tag = tags.get(0);
			return populateTagPageModelAndView(tag, showBroken);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		Tag tag = tags.get(0);
		List<TagContentCount> relatedTagLinks = relatedTagsService.getRelatedTagLinks(tag, showBroken);
		if (relatedTagLinks.size() > 0) {
			mv.addObject("related_tags", relatedTagLinks);
		}
		
		populateCommentedTaggedNewsitems(mv, tag, showBroken);
		mv.addObject("last_changed", resourceDAO.getLastLiveTimeForTag(tag));
		populateRelatedFeed(mv, tag);
		populateGeocoded(mv, showBroken, tag);		
		populateTagFlickrPool(mv, tag);
	}
 	
	

    private void populateTagFlickrPool(ModelAndView mv, Tag tag) {
        if (tag.getFlickrCount() > 0) {
            mv.addObject("flickr_count", tag.getFlickrCount());
            mv.addObject("escaped_flickr_group_id", UrlFilters.encode(configDAO.getFlickrPoolGroupId()));
        }
    }
	
	private ModelAndView populateTagPageModelAndView(Tag tag, boolean showBroken) {		
		ModelAndView mv = new ModelAndView();				
		mv.addObject("tag", tag);
		mv.addObject("heading", tag.getDisplayName());        		
		mv.addObject("description", rssUrlBuilder.getRssDescriptionForTag(tag));
		mv.addObject("link", urlBuilder.getTagUrl(tag));	
		
		final List<Website> taggedWebsites = resourceDAO.getTaggedWebsites(tag, showBroken, MAX_WEBSITES);
		final List<Resource> taggedNewsitems = resourceDAO.getTaggedNewitems(tag, showBroken, MAX_NEWSITEMS);         
		
		mv.addObject("main_content", taggedNewsitems);
		mv.addObject("websites", taggedWebsites);
	
		if (taggedNewsitems.size() > 0) {
			 setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag));
		}
				
		mv.setViewName("tag");
		return mv;
	}
	
	

    private void populateCommentedTaggedNewsitems(ModelAndView mv, Tag tag, boolean showBroken) {
        List<Resource> allCommentedNewsitems = resourceDAO.getCommentedNewsitemsForTag(tag, showBroken, MAX_NUMBER_OF_COMMENTED_TO_SHOW + 1);        
        List<Resource>commentedToShow;
        if (allCommentedNewsitems.size() <= MAX_NUMBER_OF_COMMENTED_TO_SHOW) {
            commentedToShow = allCommentedNewsitems;            
        } else {
            commentedToShow = allCommentedNewsitems.subList(0, MAX_NUMBER_OF_COMMENTED_TO_SHOW_IN_RHS);
            final String moreCommentsUrl = urlBuilder.getTagCommentUrl(tag);
            mv.addObject("commented_newsitems_moreurl", moreCommentsUrl);
            // TODO count
        }        
        mv.addObject("commented_newsitems", commentedToShow);        
        mv.addObject("tag_watchlist", resourceDAO.getTagWatchlist(tag, showBroken));        
    }
	
    
    private void populateGeocoded(ModelAndView mv, boolean showBroken, Tag tag) {
        List<Resource> geocoded = resourceDAO.getAllValidGeocodedForTag(tag, MAX_NUMBER_OF_GEOTAGGED_TO_SHOW, showBroken);
        log.info("Found " + geocoded.size() + " valid geocoded resources for tag: " + tag.getName());      
        if (geocoded.size() > 0) {
            mv.addObject("geocoded", geocoded);
        }
    }
	
    
    private void populateRelatedFeed(ModelAndView mv, Tag tag) {       
        Feed relatedFeed = tag.getRelatedFeed();
        if (relatedFeed != null) {
            log.info("Related feed is: " + relatedFeed.getName());
            List<FeedNewsitem> relatedFeedItems = rssfeedNewsitemService.getFeedNewsitems(relatedFeed);
            mv.addObject("related_feed", relatedFeed);   
            mv.addObject("related_feed_items", relatedFeedItems);            
        } else {
            log.debug("No related feed.");
        }
    }
    
}
