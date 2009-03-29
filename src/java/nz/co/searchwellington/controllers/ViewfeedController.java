package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.feeds.FeedReader;
import nz.co.searchwellington.feeds.rss.RssPrefetcher;
import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.decoraters.editing.EditableFeedItemWrapper;
import nz.co.searchwellington.model.decoraters.editing.EditableFeedWrapper;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.FeedRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SupressionRepository;
import nz.co.searchwellington.statistics.StatsTracking;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.sun.syndication.io.FeedException;


public class ViewfeedController extends BaseMultiActionController {
    
    Logger log = Logger.getLogger(ViewfeedController.class);
    
    private RequestFilter requestFilter;
    private FeedRepository feedDAO;
    private SupressionRepository supressionDAO;
    private FeedReader feedReader;
	private RssPrefetcher rssPrefetcher;
    private UrlBuilder urlBuilder;
    
    
    public ViewfeedController(ResourceRepository resourceDAO, RequestFilter requestFilter, FeedRepository feedDAO, ItemMaker itemMaker, UrlStack urlStack, SupressionRepository supressionDAO, ConfigRepository configDAO, FeedReader feedReader, RssPrefetcher rssPrefetcher, UrlBuilder urlBuilder) {
        this.resourceDAO = resourceDAO;
        this.requestFilter = requestFilter;
        this.feedDAO = feedDAO;
        this.itemMaker = itemMaker;
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
    
    
    @Transactional
    public ModelAndView viewfeed(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        ModelAndView mv = new ModelAndView();

        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
                
        mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());
        
        requestFilter.loadAttributesOntoRequest(request);

        Feed feed = null;
        if (request.getAttribute("feedAttribute") != null) {
            feed = (Feed) request.getAttribute("feedAttribute");
        }

        if (feed != null) {                       
            if (loggedInUser != null) {
                log.info("Wrapping feed with EditableFeedWrapper:" + feed.getName());
                mv.addObject("feed", new EditableFeedWrapper(feed));                
            } else {
                mv.addObject("feed", feed);
            }
            
            
            List<Resource> feedNewsitems = feedDAO.getFeedNewsitems(feed);
            if (feedNewsitems != null && feedNewsitems.size() > 0) {
                mv.addObject("main_content", setEditUrls(feedNewsitems, feed, loggedInUser));
            } else {
              log.warn("No newsitems were loaded from feed: " + feed.getName());
            }
            
            setRss(mv, feed.getName(), feed.getUrl());
            
        } else {
            throw new IllegalArgumentException("Invalid Feed identifier.");
        }
      
        populateSecondaryFeeds(mv, loggedInUser);

        mv.setViewName("viewfeed");
        return mv;
    }

    
    protected List<Resource> setEditUrls(List<Resource> resources, Feed feed, User loggedInUser) {        
        if (loggedInUser == null) {
            return resources;
        }        
    	List< Resource> items = new ArrayList<Resource>();        
    	int itemCounter = 1;
        for (Resource feedNewsitem : resources) {
        	Resource localCopy = resourceDAO.loadResourceByUrl(feedNewsitem.getUrl());
        	boolean isSupressed =  supressionDAO.isSupressed(feedNewsitem.getUrl());        	
        	items.add(new EditableFeedItemWrapper((Newsitem) feedNewsitem, localCopy, isSupressed, feed.getId(), itemCounter));
        	itemCounter++;
		}                
        return items;        
    }

    private void decacheFeed(Feed feed) {
    	rssPrefetcher.decacheAndLoad(feed.getUrl());
    }
    
}
