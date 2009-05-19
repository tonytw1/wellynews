package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.TagContentCount;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.tagging.TagInformationService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;


public abstract class BaseMultiActionController extends MultiActionController {
    
    
    Logger log = Logger.getLogger(BaseMultiActionController.class);
    

    final protected int MAX_SECONDARY_ITEMS = 4;
    final protected int MAX_NEWSITEMS = 30;
    final protected int MAX_EVENTS_TO_SHOW_ON_FRONT = 10;
    
    protected ResourceRepository resourceDAO;   
    protected UrlStack urlStack;
    protected ConfigRepository configDAO;
    protected LoggedInUserFilter loggedInUserFilter;
    
    final protected void setRss(ModelAndView mv, String url) {
        mv.addObject("rss_url", url);
    }
   
    final protected void setRss(ModelAndView mv, String title, String url) {
        mv.addObject("rss_title", title);
        mv.addObject("rss_url", url);
    }
    
   
    final protected void populateSecondaryLatestNewsitems(ModelAndView mv, User loggedInUser) {
        boolean showBroken = loggedInUser != null;      
        final int numberOfItems = 5;
        final List<Resource> latestNewsitems = resourceDAO.getLatestNewsitems(numberOfItems, showBroken);        
        mv.addObject("latest_newsitems", latestNewsitems);
        mv.addObject("latest_newsitems_moreurl", "index#newslog");
    }
    
  
    final protected void populateArchiveLinks(ModelAndView mv, User loggedInUser, List<ArchiveLink> archiveMonths) {                        
        final int MAX_BACK_ISSUES = 6;
        if (archiveMonths.size() <= MAX_BACK_ISSUES) {
            mv.addObject("archive_links", archiveMonths);
        } else {
            mv.addObject("archive_links", archiveMonths.subList(0, MAX_BACK_ISSUES));           
        }
        boolean showBroken = loggedInUser != null;
        populateContentCounts(mv, showBroken);
    }


    private void populateContentCounts(ModelAndView mv, boolean showBroken) {      
        mv.addObject("site_count",  resourceDAO.getWebsiteCount(showBroken));
        mv.addObject("newsitem_count",  resourceDAO.getNewsitemCount(showBroken));
        mv.addObject("comment_count",  resourceDAO.getCommentedNewsitemsCount(showBroken));
    }
    


    final protected void populateSecondaryFeeds(ModelAndView mv, User loggedInUser) {      
        mv.addObject("righthand_heading", "Local Feeds");                
        mv.addObject("righthand_description", "Recently updated feeds from local organisations.");
        
        final List<Feed> allFeeds = resourceDAO.getAllFeeds();                       
        if (allFeeds.size() > 0) {
            mv.addObject("righthand_content", allFeeds);             
        } 
    }

    
    @SuppressWarnings("unchecked")
    protected void populateUntaggedNewsitem(ModelAndView mv, User loggedInUser) throws IOException {        
        List <Newsitem> untaggedNewsitems = resourceDAO.getRecentUntaggedNewsitems(); 
        if (untaggedNewsitems.size() > 0) {
            List<Newsitem> untaggedItems = new ArrayList<Newsitem>();
            for (int i = 0; i < 2; i++) {
                int randomIndex = new Random().nextInt(untaggedNewsitems.size());    
                untaggedItems.add(untaggedNewsitems.get(randomIndex));
                untaggedNewsitems.remove(randomIndex);                      
            }
            mv.getModel().put("tagless", untaggedItems);
        }
        
        // TODO seperate method
        List<Resource> recentNewsitems = resourceDAO.getLatestNewsitems(100, loggedInUser != null);
        if (recentNewsitems.size() > 0) {
        	// TODO inject
        	TagInformationService tagInformationService = new TagInformationService();
            int percentageUntagged = tagInformationService.getPercentageUntagged(recentNewsitems);
            int percentageTagged = 100 - percentageUntagged;
            log.debug("Tagged = " + percentageTagged + "%");           
            mv.addObject("tagging_success_chart", percentageTagged);
        }
    }



	
    
    
    @SuppressWarnings("unchecked")
    protected void populateAds(HttpServletRequest request, ModelAndView mv, boolean showBroken) {
        if (!showBroken) {
            mv.getModel().put("show_ads", "1");
        }
    }
    
    
    @SuppressWarnings("unchecked")
    protected void populateNewslogLastUpdated(ModelAndView mv) {
        Date latestChange = resourceDAO.getNewslogLastChanged();
        if (latestChange != null) {
            mv.getModel().put("last_updated", latestChange);
        }
    }
    
    
    protected void populateLatestGeocoded(ModelAndView mv, User loggedInUser) throws IOException {
        boolean showBroken = loggedInUser != null;
        List<Resource> geocoded = resourceDAO.getAllValidGeocoded(10, showBroken);
        log.info("Found " + geocoded.size() + " valid geocoded resources.");                
        if (geocoded.size() > 0) {
            mv.addObject("geocoded", geocoded);
            mv.addObject("geotags_is_small", 1);            
        }
    }



        
}
