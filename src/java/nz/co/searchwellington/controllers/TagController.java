package nz.co.searchwellington.controllers;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.Event;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.EventsDAO;
import nz.co.searchwellington.repositories.FeedRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.statistics.StatsTracking;
import nz.co.searchwellington.utils.GoogleMapsDisplayCleaner;
import nz.co.searchwellington.utils.UrlFilters;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;


public class TagController extends BaseMultiActionController {

    private static final int MAX_NUMBER_OF_COMMENTED_TO_SHOW = 3;
    
    private RequestFilter requestFilter;
    private FeedRepository feedDAO;  
    private EventsDAO eventsDAO;
    private RssUrlBuilder rssUrlBuilder;
    private RelatedTagsService relatedTagsService;
    private ContentModelBuilderService contentModelBuilder;

    final private int MAX_WEBSITES = 100;


    public TagController(ResourceRepository resourceDAO, RequestFilter requestFilter, ItemMaker itemMaker, UrlStack urlStack, ConfigRepository configDAO, FeedRepository feedDAO, EventsDAO eventsDAO, RssUrlBuilder rssUrlBuilder, RelatedTagsService relatedTagsService, ContentModelBuilderService contentModelBuilder) {     
        this.resourceDAO = resourceDAO;    
        this.requestFilter = requestFilter;
        this.itemMaker = itemMaker;
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.feedDAO = feedDAO;     
        this.eventsDAO = eventsDAO;
        this.rssUrlBuilder = rssUrlBuilder;
        this.relatedTagsService = relatedTagsService;
        this.contentModelBuilder = contentModelBuilder;        
    }
    
       
	public ModelAndView normal(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        logger.info("Starting normal content");                                  
        requestFilter.loadAttributesOntoRequest(request);                
		ModelAndView mv = contentModelBuilder.populateContentModel(request);
		if (mv != null) {
			addCommonModelElements(mv);
			return mv;
		}
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;
    }
	
    private void addCommonModelElements(ModelAndView mv) {
		mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());		
	}


    
    
    
    
	public ModelAndView comment(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        ModelAndView mv = new ModelAndView();        
        mv.setViewName("tagComment");
                
        User loggedInUser = setLoginState(request, mv);
        boolean showBroken = loggedInUser != null;
        
        requestFilter.loadAttributesOntoRequest(request);
        mv.addObject("tags", request.getAttribute("tags"));
        
        if (request.getAttribute("tag") != null) {
            Tag tag = (Tag) request.getAttribute("tag");
            populateCommon(request, mv, showBroken, loggedInUser, tag);

            mv.addObject("tag", tag);
            mv.addObject("heading", tag.getDisplayName() + " related comment");
            // TODO want areaname back in here.
            mv.addObject("description", tag.getDisplayName() + " listings");                        
            populateTagFlickrPool(mv, tag);
            
            final List<Resource> allCommentedForTag = resourceDAO.getCommentedNewsitemsForTag(tag, showBroken, 500);
            mv.addObject("main_content", itemMaker.setEditUrls(allCommentedForTag, loggedInUser));
            mv.addObject("main_heading", null);

            populateSecondaryLatestNewsitems(mv, loggedInUser);

        } else {
            throw new RuntimeException("Invalid tag name.");            
        }
        
        return mv;        
    }
        
    
    // TODO duplication with simple
    protected void populateGeocoded(ModelAndView mv, boolean showBroken, Resource selected, Tag tag) throws IOException {
        List<Resource> geocoded = resourceDAO.getAllValidGeocodedForTag(tag, 50, showBroken);
        log.info("Found " + geocoded.size() + " valid geocoded resources.");        
        if (selected != null && !geocoded.contains(selected)) {
        	geocoded.add(selected);
        }
                
        if (geocoded.size() > 0) {
            mv.addObject("main_content", geocoded);
            // TODO inject
            GoogleMapsDisplayCleaner cleaner = new GoogleMapsDisplayCleaner();
            mv.addObject("geocoded", cleaner.dedupe(geocoded, selected));  
            //setRss(mv, "Geocoded newsitems RSS Feed", siteInformation.getUrl() + "/rss/geotagged");
        }
    }
    
    
    public ModelAndView geotagged(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        ModelAndView mv = new ModelAndView();        
        mv.setViewName("geotagged");        
        
        User loggedInUser = setLoginState(request, mv);
        boolean showBroken = loggedInUser != null;
        
        requestFilter.loadAttributesOntoRequest(request);
        mv.addObject("tags", request.getAttribute("tags"));
        
        if (request.getAttribute("tag") != null) {
            Tag tag = (Tag) request.getAttribute("tag");
            populateCommon(request, mv, showBroken, loggedInUser, tag);

            mv.addObject("geotagged_tags", resourceDAO.getGeotaggedTags(showBroken));   
            
            mv.addObject("tag", tag);
            mv.addObject("heading", tag.getDisplayName() + " related geotagged");
            // TODO want areaname back in here.
            mv.addObject("description", tag.getDisplayName() + " listings");                        
            populateTagFlickrPool(mv, tag);
            
            mv.addObject("main_heading", null);
            populateGeocoded(mv, showBroken, null, tag);
            populateSecondaryLatestNewsitems(mv, loggedInUser);

        } else {
            throw new RuntimeException("Invalid tag name.");            
        }
        
        return mv;        
    }    
   
    
    
    

    public ModelAndView newsArchive(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        ModelAndView mv = new ModelAndView();        
        mv.setViewName("tagNewsArchive");
                
        User loggedInUser = setLoginState(request, mv);
        boolean showBroken = loggedInUser != null;
        
        requestFilter.loadAttributesOntoRequest(request);
        mv.addObject("tags", request.getAttribute("tags"));
        
        if (request.getAttribute("tag") != null) {
            Tag tag = (Tag) request.getAttribute("tag");
            populateCommon(request, mv, showBroken, loggedInUser, tag);

            mv.addObject("tag", tag);
            mv.addObject("heading", tag.getDisplayName() + " related newsitems");
            mv.addObject("description", tag.getDisplayName() + " related newsitems.");            
            
            populateTagFlickrPool(mv, tag);
            
            final List<Resource> allTagNewsitems = resourceDAO.getTaggedNewitems(tag, showBroken, 500);
            mv.addObject("main_content", itemMaker.setEditUrls(allTagNewsitems, loggedInUser));
            mv.addObject("main_heading", null);

            if (allTagNewsitems.size() > 0) {               
                setTagRss(mv, tag);               
            }
            populateSecondaryLatestNewsitems(mv, loggedInUser);

        } else {
            throw new RuntimeException("Invalid tag name.");            
        }
        
        return mv;
    }


    private void setTagRss(ModelAndView mv, Tag tag) {
        setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag));
    }
    
    
    
    
    
    
   
    

    
	


  
	private void populateGeocodedForTag(ModelAndView mv, User loggedInUser, Tag tag) throws IOException {
        boolean showBroken = loggedInUser != null;
        List<Resource> geocoded = resourceDAO.getAllValidGeocodedForTag(tag, 10, showBroken);
        log.info("Found " + geocoded.size() + " valid geocoded resources for tag: " + tag.getName());                
        if (geocoded.size() > 0) {
            // TODO inject
            GoogleMapsDisplayCleaner cleaner = new GoogleMapsDisplayCleaner();
            mv.addObject("geocoded", cleaner.dedupe(geocoded));
            mv.addObject("geotags_is_small", 1);     
        }
    }


  
    protected void populateTagEditUrl(ModelAndView mv, Tag tag) {
        // TODO migrate away from parmeters to path.
        final String editUrl = "edit/tag/" + UrlFilters.encode(tag.getName());
        mv.addObject("editurl", editUrl);
    }

    
    protected void populateTagDeleteUrl(ModelAndView mv, Tag tag) {
        // TODO migrate away from parmeters to path.
        final String deleteUrl = "delete/tag/" + UrlFilters.encode(tag.getName());
        mv.addObject("deleteurl", deleteUrl);
    }

  
    protected void populatePlacesAutotagUrl(ModelAndView mv, Tag tag) {
        final String autoTagUrl = "autotag/tag/" + UrlFilters.encode(tag.getName());
        mv.addObject("run_places_autotagger_url", autoTagUrl);
    }
    
    
     private void populateCommon(HttpServletRequest request, ModelAndView mv, boolean showBroken, User loggedInUser, Tag tag) {
        urlStack.setUrlStack(request);
        populateAds(request, mv, showBroken);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
       
        mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());        
                
        if (tag != null && tag.getName().equals("realestate")) {      
        	mv.addObject("use_big_ads", 1);
        }
    }


    private void populateTagFlickrPool(ModelAndView mv, Tag tag) {
        if (tag.getFlickrCount() > 0) {
            mv.addObject("flickr_count", tag.getFlickrCount());
            mv.addObject("escaped_flickr_group_id", UrlFilters.encode(configDAO.getFlickrPoolGroupId()));
        }
    }


   
    
    
    

}
