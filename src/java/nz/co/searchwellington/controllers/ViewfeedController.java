package nz.co.searchwellington.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
import nz.co.searchwellington.feeds.FeedReader;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.feeds.rss.RssPrefetcher;
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
public class ViewfeedController extends BaseMultiActionController {
    
    Logger log = Logger.getLogger(ViewfeedController.class);
    
    private AdminRequestFilter requestFilter;
    private LoggedInUserFilter loggedInUserFilter;
    private RssfeedNewsitemService rssfeedNewsitemService;
    private SupressionRepository supressionDAO;
    private FeedReader feedReader;
	private RssPrefetcher rssPrefetcher;
    private UrlBuilder urlBuilder;
    
    
    public ViewfeedController(ResourceRepository resourceDAO, AdminRequestFilter requestFilter,  LoggedInUserFilter loggedInUserFilter, RssfeedNewsitemService rssfeedNewsitemService, UrlStack urlStack, SupressionRepository supressionDAO, ConfigRepository configDAO, FeedReader feedReader, RssPrefetcher rssPrefetcher, UrlBuilder urlBuilder) {
        this.resourceDAO = resourceDAO;
        this.requestFilter = requestFilter;
        this.loggedInUserFilter = loggedInUserFilter;
        this.rssfeedNewsitemService = rssfeedNewsitemService;       
        this.urlStack = urlStack;
        this.supressionDAO = supressionDAO;
        this.configDAO = configDAO;        
        this.feedReader = feedReader;
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
            feedReader.processFeed(feed);            
        } else {
            log.info("No feed seen on request; nothing to reread.");
        }        
        return new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)));   
    }
    
    
   
    
    private void decacheFeed(Feed feed) {
    	rssPrefetcher.decacheAndLoad(feed.getUrl());
    }
    
}
