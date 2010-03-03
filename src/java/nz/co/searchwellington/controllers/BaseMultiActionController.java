package nz.co.searchwellington.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;


public abstract class BaseMultiActionController extends MultiActionController {
    
    
    Logger log = Logger.getLogger(BaseMultiActionController.class);
    

    final protected int MAX_SECONDARY_ITEMS = 4;
    final protected int MAX_NEWSITEMS = 30;
    final protected int MAX_EVENTS_TO_SHOW_ON_FRONT = 10;
    
    protected UrlStack urlStack;
    protected ConfigRepository configDAO;
    protected LoggedInUserFilter loggedInUserFilter;
    protected ContentRetrievalService contentRetrievalService;
    
    
    final protected void setRss(ModelAndView mv, String url) {
        mv.addObject("rss_url", url);
    }
   
    final protected void setRss(ModelAndView mv, String title, String url) {
        mv.addObject("rss_title", title);
        mv.addObject("rss_url", url);
    }
    
   
    // TODO migrate all inlines to use this.
    final protected void populateCommonLocal(ModelAndView mv) {      
        mv.addObject("top_level_tags", contentRetrievalService.getTopLevelTags());      
    }
    
    
    final protected void populateSecondaryLatestNewsitems(ModelAndView mv) {            
        final int numberOfItems = 5;
        final List<Resource> latestNewsitems = contentRetrievalService.getLatestNewsitems(numberOfItems);        
        mv.addObject("latest_newsitems", latestNewsitems);
        mv.addObject("latest_newsitems_moreurl", "index#newslog");
    }
    

    final protected void populateSecondaryFeeds(ModelAndView mv, User loggedInUser) {      
        mv.addObject("righthand_heading", "Feeds");    
        mv.addObject("righthand_description", "Recently updated feeds.");   
        final List<Resource> allFeeds = contentRetrievalService.getAllFeeds();
        if (allFeeds.size() > 0) {
            mv.addObject("righthand_content", allFeeds);             
        } 
    }
    
}
