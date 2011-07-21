package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.feeds.FeedReader;
import nz.co.searchwellington.feeds.rss.RssNewsitemPrefetcher;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

import com.sun.syndication.io.FeedException;

// TODO move to admin.
public class AdminFeedController extends MultiActionController {
    
    private static Logger log = Logger.getLogger(AdminFeedController.class);
    
    private AdminRequestFilter requestFilter;
    private FeedReader feedReader;    
	private RssNewsitemPrefetcher rssPrefetcher;
    private UrlBuilder urlBuilder;
    private EditPermissionService permissionService;
    private LoggedInUserFilter loggedInUserFilter;
    
    public AdminFeedController(AdminRequestFilter requestFilter,
			FeedReader feedReader,
			RssNewsitemPrefetcher rssPrefetcher, UrlBuilder urlBuilder,
			EditPermissionService permissionService, LoggedInUserFilter loggedInUserFilter) {
		this.requestFilter = requestFilter;
		this.feedReader = feedReader;
		this.rssPrefetcher = rssPrefetcher;
		this.urlBuilder = urlBuilder;
		this.permissionService = permissionService;
		this.loggedInUserFilter = loggedInUserFilter;
	}
    
    @Transactional   
    public ModelAndView decachefeed(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {          
        requestFilter.loadAttributesOntoRequest(request);
        Feed feed = null;
        if (request.getAttribute("feedAttribute") != null) {
            feed = (Feed) request.getAttribute("feedAttribute");
            
            if (!permissionService.canDecache(feed)) {
            	log.warn("Not allowed to decache this feed"); // TODO return http auth error
            	return null;            	
            }
                        
            log.info("Decaching feed: " + feed.getName());
            rssPrefetcher.decacheAndLoad(feed);
        } else {
            log.info("No feed seen on request; nothing to decache.");            
        }        
        return new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)));
    }
            
    @Transactional
    public ModelAndView acceptAllFrom(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        requestFilter.loadAttributesOntoRequest(request);
        Feed feed = null;
        if (request.getAttribute("feedAttribute") != null) {
            feed = (Feed) request.getAttribute("feedAttribute");
            
            if (!permissionService.canAcceptAllFrom(feed)) {
            	log.warn("Not allowed to read this feed"); // TODO return http auth error
            	return null; 	
            }
            
            log.info("Reading feed: " + feed.getName());           
            feedReader.processFeed(feed.getId(), loggedInUserFilter.getLoggedInUser());
            
        } else {
            log.info("No feed seen on request; nothing to reread.");
        }        
        return new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)));   
    }
    
}
