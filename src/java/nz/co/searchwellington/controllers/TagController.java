package nz.co.searchwellington.controllers;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.Event;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.TagContentCount;
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
import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;


public class TagController extends BaseMultiActionController {

    private static final int MAX_NUMBER_OF_COMMENTED_TO_SHOW = 3;

    Logger log = Logger.getLogger(TagController.class);
    
    private RequestFilter requestFilter;
    private FeedRepository feedDAO;  
    private EventsDAO eventsDAO;
    private RssUrlBuilder rssUrlBuilder;
    
    final private int MAX_WEBSITES = 100;

    
    public TagController(ResourceRepository resourceDAO, RequestFilter requestFilter, ItemMaker itemMaker, UrlStack urlStack, ConfigRepository configDAO, FeedRepository feedDAO, EventsDAO eventsDAO, RssUrlBuilder rssUrlBuilder) {     
        this.resourceDAO = resourceDAO;    
        this.requestFilter = requestFilter;
        this.itemMaker = itemMaker;
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.feedDAO = feedDAO;     
        this.eventsDAO = eventsDAO;
        this.rssUrlBuilder = rssUrlBuilder;
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
        setRss(mv, rssUrlBuilder.getRssUrlForTag(tag));
    }
    
   
    @SuppressWarnings("unchecked")
	public ModelAndView normal(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        logger.info("Starting normal tag.");
        ModelAndView mv = new ModelAndView();
        mv.setViewName("tag");

        User loggedInUser = setLoginState(request, mv);
        boolean showBroken = loggedInUser != null;
            
        populateSecondaryLatestNewsitems(mv, loggedInUser);                        
        requestFilter.loadAttributesOntoRequest(request);
        
        List<Tag> tags = (List<Tag>) request.getAttribute("tags");
        log.info("Tags: " + tags);
        
        if (tags.size() == 1) {

            Tag tag = null;
            final Tag firstTag = tags.get(0);
            tag = firstTag;
            populateTagFlickrPool(mv, tag);
            populateCommon(request, mv, showBroken, loggedInUser, tag);
            
            mv.addObject("tag", tag);
            mv.addObject("heading", tag.getDisplayName());
            mv.addObject("description", tag.getDisplayName() + " listings.");
          
            mv.addObject("last_changed", resourceDAO.getLastLiveTimeForTag(tag));
            
            populateTagImages(mv, tag);

            final List<Website> taggedWebsites = resourceDAO.getTaggedWebsites(tag, showBroken, MAX_WEBSITES);
           
            List<Resource> commentedNewsitemOnPage = populateCommentedTaggedNewsitems(mv, tag, showBroken, loggedInUser);
            List<Resource> taggedNewsitems = resourceDAO.getTaggedNewitems(tag, showBroken, MAX_NEWSITEMS + commentedNewsitemOnPage.size());
                       
            // RSS
            if (taggedNewsitems.size() > 0) {
                setTagRss(mv, tag);               
            }
            
            if (tag.getRelatedTwitter() != null) {
            	mv.addObject("twitterUsername", tag.getRelatedTwitter());
            }
            
            // Edit
            if (loggedInUser != null) {             
                populateTagEditUrl(mv, tag);
                populateTagDeleteUrl(mv, tag);
                populatePlacesAutotagUrl(mv, tag);                
            }

            populateRelatedTagLinks(mv, showBroken, tag);
            
            populateRelatedFeed(mv, tag, feedDAO);
            
            List <Resource> tagCalendars = resourceDAO.getCalendarFeedsForTag(tag);
            populateRelatedCalendars(mv, tagCalendars, loggedInUser);
            populateRelatedEvents(mv, tagCalendars);
            
            
            final int allAvailableNewsitems = resourceDAO.getTaggedNewitemsCount(tag, showBroken);            
            
            final boolean showMainAndSecondaryContent = populateMainAndSecondaryContent(mv, loggedInUser, tag, taggedWebsites, allAvailableNewsitems, taggedNewsitems, tag.getDisplayName(), commentedNewsitemOnPage);
            if (!showMainAndSecondaryContent) {
                mv.setViewName("tagOneContentType");
                mv.addObject("main_heading", null);
            }

            
            if (tag.getParent() != null) {
                mv.addObject("parent_tag", tag.getParent());
                List tagSiblings = new ArrayList<Tag>(tag.getParent().getChildren());
                tagSiblings.remove(tag);
                mv.addObject("sibling_tags", tagSiblings);
            }
                        
            populateGeocodedForTag(mv, loggedInUser, tag);

            mv.addObject("tag_watchlist", itemMaker.setEditUrls(resourceDAO.getTagWatchlist(tag, showBroken), loggedInUser));
   
        } else if (tags.size() == 2) {
            mv.addObject("tags", tags);
            
            final Tag firstTag = tags.get(0);
            populateTagFlickrPool(mv, firstTag);
            populateCommon(request, mv, showBroken, loggedInUser, firstTag);
            
            final String tagsTitle = firstTag.getDisplayName() + " and " + tags.get(1).getDisplayName();
            mv.addObject("heading", tagsTitle);
            mv.addObject("description", tagsTitle + " listings.");
            
            final List<Website> taggedWebsites = resourceDAO.getTaggedWebsites(new HashSet<Tag>(tags), showBroken, MAX_WEBSITES);  
            final List<Resource> taggedNewsitems = resourceDAO.getTaggedNewsitems(new HashSet<Tag>(tags), showBroken, MAX_WEBSITES);
       
            // TODO can you have commented newsitems on a combiner page?
            final boolean showMainAndSecondaryContent = populateMainAndSecondaryContent(mv, loggedInUser, firstTag, taggedWebsites, taggedWebsites.size(), taggedNewsitems, tagsTitle, null);
            
            
            if (!showMainAndSecondaryContent) {
                mv.setViewName("tagCombinedOneContentType");              
            }
            
            
            populateRelatedTagLinks(mv, showBroken, firstTag);
            
                  
            mv.addObject("tag", firstTag);
            
        } else {
            // TODO return a 404; how do you do this from spring?  
            throw new RuntimeException("Invalid tag name.");
        }

        logger.info("Finishing normal tag.");
        return mv;
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





    private void populateRelatedCalendars(ModelAndView mv, List<Resource> tagCalendars, User loggedInUser) throws IOException {             
        if (tagCalendars.size() > 0) {            
            List<Resource> wrappedFeeds = itemMaker.wrapCalendars(tagCalendars);
            mv.addObject("tag_calendars", itemMaker.setEditUrls(wrappedFeeds, loggedInUser));
            
            mv.addObject("tag_calendars_moreurl", "/calendars");
        }    
	}


    private void populateRelatedEvents(ModelAndView mv, List<Resource> tagCalendars) throws IOException {    	
        List<Event> tagEvents = eventsDAO.getPendingEventsForTag(tagCalendars, MAX_EVENTS_TO_SHOW_ON_FRONT);
          if (tagEvents.size() > 0) {
              mv.addObject("tag_events", tagEvents);
          }
    }






   






    private List<Resource> dedupeTaggedNewsitems(List<Resource> commentedNewsitemOnPage, List<Resource> taggedNewsitems, List<Website> taggedWebsites) {      
        // TODO inject
        ContentDedupingService dedupingService = new ContentDedupingService();        
        dedupingService.dedupeTagPageNewsitems(taggedNewsitems, commentedNewsitemOnPage, taggedWebsites);
        
        // TODO move to deduping service.
        if (taggedNewsitems.size() > MAX_NEWSITEMS) {
           return taggedNewsitems.subList(0, MAX_NEWSITEMS);
        } else {        
            return taggedNewsitems;
        }
        
    }



    private void populateRelatedTagLinks(ModelAndView mv, boolean showBroken, Tag tag) throws IOException {
        // TODO move all of this lot to the DAO.
        List<Tag> relatedTags = resourceDAO.getRelatedLinksForTag(tag, showBroken);
        List<TagContentCount> relatedTagLinks = new ArrayList<TagContentCount>();
        for (Tag relatedTag : relatedTags) {
            // TODO is there a count only call for this?
            int relatedItemCount = resourceDAO.getTaggedWebsites(new HashSet<Tag>(Arrays.asList(tag, relatedTag)), showBroken, MAX_WEBSITES).size();
                        
            // TODO merge these calls to get a speed up.
            relatedItemCount = relatedItemCount + resourceDAO.getTaggedNewsitems(new HashSet<Tag>(Arrays.asList(tag, relatedTag)), showBroken, MAX_WEBSITES).size();
            relatedTagLinks.add(new TagContentCount(relatedTag, relatedItemCount));                        
        }
                
        Collections.sort(relatedTagLinks);        
        mv.addObject("related_tags", relatedTagLinks);
    }



    @SuppressWarnings("unchecked")
    private List<Resource> populateCommentedTaggedNewsitems(ModelAndView mv, Tag tag, boolean showBroken, User loggedInUser) throws IOException {
        List<Resource> allCommentedNewsitems = resourceDAO.getCommentedNewsitemsForTag(tag, showBroken, 500);
        
        List<Resource>commentedToShow;
        if (allCommentedNewsitems.size() <= MAX_NUMBER_OF_COMMENTED_TO_SHOW) {
            commentedToShow = allCommentedNewsitems;            
        } else {
            commentedToShow = allCommentedNewsitems.subList(0, MAX_NUMBER_OF_COMMENTED_TO_SHOW);
            final String moreCommentsUrl = "/tag/" + tag.getName() + "/comment";
            mv.addObject("commented_newsitems_moreurl", moreCommentsUrl);
            mv.addObject("commented_newsitems_morecount", new Integer(allCommentedNewsitems.size() - MAX_NUMBER_OF_COMMENTED_TO_SHOW));
        }
        
        mv.addObject("commented_newsitems", itemMaker.setEditUrls(commentedToShow, loggedInUser));
        return commentedToShow;             
    }



    @SuppressWarnings("unchecked")
    protected void populateTagEditUrl(ModelAndView mv, Tag tag) {
        // TODO migrate away from parmeters to path.
        final String editUrl = "/edit/tag/" + UrlFilters.encode(tag.getName());
        mv.addObject("editurl", editUrl);
    }


    @SuppressWarnings("unchecked")
    protected void populateTagDeleteUrl(ModelAndView mv, Tag tag) {
        // TODO migrate away from parmeters to path.
        final String deleteUrl = "/delete/tag/" + UrlFilters.encode(tag.getName());
        mv.addObject("deleteurl", deleteUrl);
    }

  

    @SuppressWarnings("unchecked")   
    protected void populatePlacesAutotagUrl(ModelAndView mv, Tag tag) {
        final String autoTagUrl = "/autotag/tag/" + UrlFilters.encode(tag.getName());
        mv.addObject("run_places_autotagger_url", autoTagUrl);
    }
    
    
    

    // TODO moving the commented items into here might help with the deduping problem
    @SuppressWarnings("unchecked")
    private boolean populateMainAndSecondaryContent(ModelAndView mv, User loggedInUser, Tag tag, final List<Website> taggedWebsites, int allAvailableNewsitems, List<Resource> taggedNewsitems, String pageName, List<Resource> commentedNewsitemOnPage) {
        List<? extends Resource> mainContent;
        List<? extends Resource> secondaryContent;
        String mainHeading;
        String secondaryHeading;
                
        // TODO dedupe is currently disabled.
        // TODO only dedupe if we are going to actually show the commented box; ie we must be in two column mode.
        if (commentedNewsitemOnPage != null) {
            dedupeTaggedNewsitems(commentedNewsitemOnPage, taggedNewsitems, taggedWebsites);
            log.debug("Found " + taggedNewsitems.size() + " deduped tagged newsitems.");
        }
        
        
        if (taggedWebsites.size() >= taggedNewsitems.size()) {
            mainContent = taggedWebsites;
            secondaryContent = taggedNewsitems;

            mainHeading = pageName + " related sites";
            secondaryHeading = "Related newsitems";

            if (allAvailableNewsitems > MAX_NEWSITEMS) {
                // set second more to point to all news.
                mv.addObject("secondary_content_moreurl", "/tag/" + tag.getName() + "/news");
                mv.addObject("secondary_content_morecount", allAvailableNewsitems - MAX_NEWSITEMS);
            }
            
        } else {
            mainContent = taggedNewsitems;
            secondaryContent = taggedWebsites;
            mainHeading = pageName + " related newsitems";
            secondaryHeading = "Related sites";

            if (allAvailableNewsitems > MAX_NEWSITEMS) {
                // set second more to point to all news.
                mv.addObject("main_content_moreurl", "/tag/" + tag.getName() + "/news");
                mv.addObject("main_content_morecount", allAvailableNewsitems - MAX_NEWSITEMS);
            }
        }

        mv.addObject("main_content", itemMaker.setEditUrls(mainContent, loggedInUser));
        mv.addObject("main_heading", mainHeading);

        mv.addObject("secondary_content", itemMaker.setEditUrls(secondaryContent, loggedInUser));
        mv.addObject("secondary_heading", secondaryHeading);
        
        if (secondaryContent.size() == 0) {
            return false;           
        }
        return true;
        
    }


     private void populateCommon(HttpServletRequest request, ModelAndView mv, boolean showBroken, User loggedInUser, Tag tag) {
        urlStack.setUrlStack(request);
        populateAds(request, mv, showBroken);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
       
        mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());        
                
        if (tag.getName().equals("realestate")) {      
        	mv.addObject("use_big_ads", 1);
        }
    }


    private void populateTagFlickrPool(ModelAndView mv, Tag tag) {
        if (tag.getFlickrCount() > 0) {
            mv.addObject("flickr_count", tag.getFlickrCount());
            mv.addObject("escaped_flickr_group_id", UrlFilters.encode(configDAO.getFlickrPoolGroupId()));
        }
    }


    private void populateTagImages(ModelAndView mv, Tag tag) {
        log.debug("Tag main image is: " + tag.getMainImage());
        if (tag.getMainImage() != null) {
            mv.addObject("tag_image", tag.getMainImage());
        }
        if (tag.getSecondaryImage() != null) {
            mv.addObject("secondary_tag_image", tag.getSecondaryImage());
        }
    }
    
    
    

}
