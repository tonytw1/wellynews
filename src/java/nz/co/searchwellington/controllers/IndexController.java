package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.Event;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.EventsDAO;
import nz.co.searchwellington.repositories.FeedRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.statistics.StatsTracking;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;


public class IndexController extends BaseMultiActionController {
        
    private static final int NUMBER_OF_COMMENTED_TO_SHOW = 4;
  


    Logger log = Logger.getLogger(IndexController.class);

    private FeedRepository feedDAO;
    private EventsDAO eventsDAO;
    private SiteInformation siteInformation;
    private RssUrlBuilder rssUrlBuilder;
    
    
    
    public IndexController(ResourceRepository resourceDAO, ItemMaker itemMaker, UrlStack urlStack, ConfigRepository configDAO, FeedRepository feedDAO, EventsDAO eventsDAO, SiteInformation siteInformation, RssUrlBuilder rssUrlBuilder) {   
        this.resourceDAO = resourceDAO;
        this.itemMaker = itemMaker;
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.feedDAO = feedDAO;
        this.eventsDAO = eventsDAO;
        this.siteInformation = siteInformation;
        this.rssUrlBuilder = rssUrlBuilder;        
    }
    

        
    @SuppressWarnings("unchecked")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws IOException, IllegalArgumentException, FeedException {
        logger.info("Starting index.");
        
        ModelAndView mv = new ModelAndView();
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        boolean showBroken = loggedInUser != null;
        populateAds(request, mv, showBroken);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
               
        populateSecondaryJustin(mv, loggedInUser);               
     
        mv.getModel().put("top_level_tags", resourceDAO.getTopLevelTags());

        // TODO dedupe between main and comments.
        List<Newsitem> commentedNewsitems = populateCommentedNewsitems(mv);
        
        final List<Newsitem> latestNewsitems = resourceDAO.getLatestNewsitems(MAX_NEWSITEMS + commentedNewsitems.size(), showBroken);                
        mv.addObject("main_content", itemMaker.setEditUrls(latestNewsitems, loggedInUser));
        
        List<ArchiveLink> archiveMonths = resourceDAO.getArchiveMonths();
		populateArchiveLinks(mv, loggedInUser, archiveMonths);
        if (monthOfLastItem(latestNewsitems) != null) {
            mv.getModel().put("main_content_moreurl", makeArchiveUrl(monthOfLastItem(latestNewsitems), archiveMonths ));
        }
               
        populateUserOwnedResource(request, mv, loggedInUser);       
        setRss(mv, "Search " + siteInformation.getAreaname() + " Newslog", rssUrlBuilder.getBaseRssUrl());
     
        mv.addObject("current_time", Calendar.getInstance().getTime());
        
        populateNewslogLastUpdated(mv);        
        populateFeatured(mv, loggedInUser);               
        populateUntaggedNewsitem(mv, loggedInUser);        
        populateEvents(mv);
        
        populateLatestGeocoded(mv, loggedInUser);
        
        mv.setViewName("index");        
        Tag relatedFeedTag = resourceDAO.loadTagByName("emergencyservices");
        if (relatedFeedTag != null) {
            populateRelatedFeed(mv, relatedFeedTag, feedDAO);
        }
        
        logger.info("Finished index.");
        return mv;
    }



    
    
    
    private void populateEvents(ModelAndView mv) throws IOException {
        List<Event> events = eventsDAO.getAllPendingEvents(MAX_EVENTS_TO_SHOW_ON_FRONT);
          if (events.size() > 0) {
              mv.addObject("events", events);
          }
    }
    

    


    @SuppressWarnings("unchecked")
    private void populateFeatured(ModelAndView mv, User loggedInUser) throws IOException {
        final Tag featuredTag = resourceDAO.loadTagByName("featured");
        if (featuredTag != null) {         
            List<Website> featuredSites = resourceDAO.getTaggedWebsites(featuredTag, false, 10);                                    
            mv.getModel().put("featured", featuredSites);     
        }
    }



    @SuppressWarnings("unchecked")
    private List<Newsitem> populateCommentedNewsitems(ModelAndView mv) {
        
        // TODO performance; would a count query method help?
        final List<Newsitem> allCommentedNewsitems = resourceDAO.getAllCommentedNewsitems(500, true);
        final List<Newsitem> recentCommentedNewsitems;
                
        if (allCommentedNewsitems.size() <= NUMBER_OF_COMMENTED_TO_SHOW) {
            recentCommentedNewsitems = allCommentedNewsitems;
        } else {
            recentCommentedNewsitems = allCommentedNewsitems.subList(0, NUMBER_OF_COMMENTED_TO_SHOW);
        }
        
        log.debug("Put " + recentCommentedNewsitems.size() + " commented newsitems onto model.");
        mv.getModel().put("commented_newsitems", recentCommentedNewsitems);
                
        final int commentedNewsitemCount = allCommentedNewsitems.size();
        if (commentedNewsitemCount > NUMBER_OF_COMMENTED_TO_SHOW) {         
            final String moreCommentsUrl = "comment";
            mv.getModel().put("commented_newsitems_moreurl", moreCommentsUrl);
            mv.getModel().put("commented_newsitems_morecount", new Integer(commentedNewsitemCount - NUMBER_OF_COMMENTED_TO_SHOW));
        }
        
        return recentCommentedNewsitems;
    }



 

    // TODO move to requestfilter and make available on all pages.
    private void populateUserOwnedResource(HttpServletRequest request, ModelAndView mv, User loggedInUser) {
        Integer ownedItemId = (Integer) request.getSession().getAttribute("owned");
        if (ownedItemId != null) {
            Resource ownedItem = resourceDAO.loadResourceById(ownedItemId);
            if (ownedItem != null) {
                log.info("Owned item put onto model: " + ownedItem.getName());
                mv.addObject("owneditem", ownedItem);
            } else {
                log.warn("Could not load owned item with id: " + ownedItemId);
            }
        }
    }


    
    
    private Date monthOfLastItem(List<Newsitem> latestNewsitems) {
        if (latestNewsitems.size() > 0) {
            Resource lastNewsitem = latestNewsitems.get(latestNewsitems.size()-1);          
            if (lastNewsitem.getDate() != null) {
                Date lastDate = lastNewsitem.getDate();
                return lastDate;
            }
        }
        return null;
    }



    
    @SuppressWarnings("unchecked")
    private void populateSecondaryJustin(ModelAndView mv, User loggedInUser) throws IOException {
        boolean showBroken = loggedInUser != null;
        mv.getModel().put("secondary_heading", "Just In");
        mv.getModel().put("secondary_description", "New additions.");
        mv.getModel().put("secondary_content", itemMaker.setEditUrls(resourceDAO.getLatestWebsites(MAX_SECONDARY_ITEMS, showBroken), loggedInUser));                  
        mv.getModel().put("secondary_content_moreurl", "justin");        
    }
    
    

    public String makeArchiveUrl(Date dateOfLastNewsitem, List<ArchiveLink> archiveMonths) {    	
    	ArchiveLink archiveLink = null;
		for (ArchiveLink monthLink : archiveMonths) {
    		boolean monthMatches = monthLink.getMonth().getMonth() == dateOfLastNewsitem.getMonth() && monthLink.getMonth().getYear() == dateOfLastNewsitem.getYear();
    		// TODO Want an early break out.
    		if (monthMatches) {
    			archiveLink = monthLink;
    		}
		}
    	if (archiveLink != null) {
    		return archiveLink.getHref();
    	}
    	return null;
    }
   

    
}
    