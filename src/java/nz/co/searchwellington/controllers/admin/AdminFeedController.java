package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.feeds.FeedReader;
import nz.co.searchwellington.feeds.FeedReaderRunner;
import nz.co.searchwellington.feeds.LiveRssfeedNewsitemService;
import nz.co.searchwellington.feeds.rss.RssNewsitemPrefetcher;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SupressionRepository;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.sun.syndication.io.FeedException;

// TODO move to admin.
public class AdminFeedController extends BaseMultiActionController {
    
    Logger log = Logger.getLogger(AdminFeedController.class);
    
    private AdminRequestFilter requestFilter;
    private LoggedInUserFilter loggedInUserFilter;
    private SupressionRepository supressionDAO;
    private FeedReaderRunner feedReaderRunner;    
	private RssNewsitemPrefetcher rssPrefetcher;
    private UrlBuilder urlBuilder;
    
    
    public AdminFeedController(ResourceRepository resourceDAO, AdminRequestFilter requestFilter,  LoggedInUserFilter loggedInUserFilter, SupressionRepository supressionDAO, 
    		ConfigRepository configDAO, FeedReaderRunner feedReaderRunner, RssNewsitemPrefetcher rssPrefetcher, UrlBuilder urlBuilder) {
        this.resourceDAO = resourceDAO;
        this.requestFilter = requestFilter;
        this.loggedInUserFilter = loggedInUserFilter;       
        this.supressionDAO = supressionDAO;
        this.configDAO = configDAO;        
        this.feedReaderRunner = feedReaderRunner;
        this.rssPrefetcher = rssPrefetcher;
        this.urlBuilder = urlBuilder;       
    }

    
    @Transactional   
    public ModelAndView decachefeed(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {          
        requestFilter.loadAttributesOntoRequest(request);
        Feed feed = null;
        if (request.getAttribute("feedAttribute") != null) {
            feed = (Feed) request.getAttribute("feedAttribute");
            log.info("Decaching feed: " + feed.getName());
            decacheFeed(feed);
        } else {
            log.info("No feed seen on request; nothing to decache.");            
        }        
        return new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)));
    }
    
        
    @Transactional
    public ModelAndView readfeed(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        requestFilter.loadAttributesOntoRequest(request);
        Feed feed = null;
        if (request.getAttribute("feedAttribute") != null) {
            feed = (Feed) request.getAttribute("feedAttribute");
            log.info("Reading feed: " + feed.getName());           
            feedReaderRunner.readSingleFeed(feed);
            
        } else {
            log.info("No feed seen on request; nothing to reread.");
        }        
        return new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)));   
    }
    
    
   
    
    private void decacheFeed(Feed feed) {
    	rssPrefetcher.decacheAndLoad(feed);
    }
    
}
