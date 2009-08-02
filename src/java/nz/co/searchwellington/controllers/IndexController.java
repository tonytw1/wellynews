package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.Event;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.EventsDAO;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;


public class IndexController extends BaseMultiActionController {
        
    private static final int NUMBER_OF_COMMENTED_TO_SHOW = 2;
    
    Logger log = Logger.getLogger(IndexController.class);

    private EventsDAO eventsDAO;
    private SiteInformation siteInformation;
    private RssUrlBuilder rssUrlBuilder;
	private LoggedInUserFilter loggedInUserFilter;
	private UrlBuilder urlBuilder;
	private RequestFilter requestFilter;    
    
    
    public IndexController(ResourceRepository resourceDAO, UrlStack urlStack, ConfigRepository configDAO, EventsDAO eventsDAO, SiteInformation siteInformation, RssUrlBuilder rssUrlBuilder, LoggedInUserFilter loggedInUserFilter, UrlBuilder urlBuilder, RequestFilter requestFilter) {   
        this.resourceDAO = resourceDAO;        
        this.urlStack = urlStack;
        this.configDAO = configDAO;       
        this.eventsDAO = eventsDAO;
        this.siteInformation = siteInformation;
        this.rssUrlBuilder = rssUrlBuilder;
        this.loggedInUserFilter = loggedInUserFilter;
        this.urlBuilder = urlBuilder;
        this.requestFilter = requestFilter;
    }
    

        
    @SuppressWarnings("unchecked")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws IOException, IllegalArgumentException, FeedException {
    	logger.info("Starting index.");    	  	
        ModelAndView mv = new ModelAndView();
        
        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean showBroken = loggedInUser != null;
        populateAds(request, mv, showBroken);    
        populateSecondaryJustin(mv, loggedInUser);               
     
        mv.getModel().put("top_level_tags", resourceDAO.getTopLevelTags());
        
        final List<Resource> latestNewsitems = resourceDAO.getLatestNewsitems(MAX_NEWSITEMS, showBroken);                
        mv.addObject("main_content", latestNewsitems);
        populateCommentedNewsitems(mv, showBroken);
        
        List<ArchiveLink> archiveMonths = resourceDAO.getArchiveMonths(showBroken);
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
            mv.getModel().put("featured", resourceDAO.getTaggedWebsites(featuredTag, false, 10));
        }
    }


    private void populateCommentedNewsitems(ModelAndView mv, boolean showBroken) { 
        final List<Resource> recentCommentedNewsitems = resourceDAO.getCommentedNewsitems(2, showBroken, true, 0);                
        if (recentCommentedNewsitems.size() <= NUMBER_OF_COMMENTED_TO_SHOW) {
        	mv.addObject("commented_newsitems", recentCommentedNewsitems);
        } else {
        	mv.addObject("commented_newsitems", recentCommentedNewsitems.subList(0, NUMBER_OF_COMMENTED_TO_SHOW));            
        }   
        mv.addObject("commented_newsitems_moreurl", "comment");        
    }

        
    private void populateUserOwnedResource(HttpServletRequest request, ModelAndView mv, User loggedInUser) {        
    	Resource ownedItem = requestFilter.getAnonResource();
    	if (ownedItem != null) {
    		log.info("Owned item put onto model: " + ownedItem.getName());
    		mv.addObject("owneditem", ownedItem);
    	}        
    }

    
    private Date monthOfLastItem(List<Resource> latestNewsitems) {
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
        mv.getModel().put("secondary_content", resourceDAO.getLatestWebsites(MAX_SECONDARY_ITEMS, showBroken));                  
        mv.getModel().put("secondary_content_moreurl", "justin");        
    }
    
    

    public String makeArchiveUrl(Date dateOfLastNewsitem, List<ArchiveLink> archiveMonths) {    	
    	ArchiveLink archiveLink = getArchiveLinkForDate(dateOfLastNewsitem, archiveMonths);
    	if (archiveLink != null) {
    		return urlBuilder.getArchiveLinkUrl(archiveLink);
    	}
    	return null;
    }



	private ArchiveLink getArchiveLinkForDate(Date dateOfLastNewsitem,
			List<ArchiveLink> archiveMonths) {
		ArchiveLink archiveLink = null;
		for (ArchiveLink monthLink : archiveMonths) {
    		boolean monthMatches = monthLink.getMonth().getMonth() == dateOfLastNewsitem.getMonth() && monthLink.getMonth().getYear() == dateOfLastNewsitem.getYear();
    		// TODO Want an early break out.
    		if (monthMatches) {
    			archiveLink = monthLink;
    		}
		}
		return archiveLink;
	}
   

    
}
    