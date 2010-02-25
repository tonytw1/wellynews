package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.feeds.DiscoveredFeedRepository;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;


public class SimplePageController extends BaseMultiActionController {
    
    private static final int MAX_TWITTERS_TO_SHOW = 12;

	Logger log = Logger.getLogger(SimplePageController.class);
    
    
    private SiteInformation siteInformation;
    private RssUrlBuilder rssUrlBuilder;
	private DiscoveredFeedRepository discoveredFeedRepository;
    private ShowBrokenDecisionService showBrokenDecisionService;
    
    
    public SimplePageController(ResourceRepository resourceDAO, UrlStack urlStack, ConfigRepository configDAO, 
    		SiteInformation siteInformation, RssUrlBuilder rssUrlBuilder, DiscoveredFeedRepository discoveredFeedRepository, 
    		ShowBrokenDecisionService showBrokenDecisionService, ContentRetrievalService contentRetrievalService) {
        this.resourceDAO = resourceDAO;
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.siteInformation = siteInformation;        
        this.rssUrlBuilder = rssUrlBuilder;
        this.discoveredFeedRepository = discoveredFeedRepository;
        this.showBrokenDecisionService = showBrokenDecisionService;
        this.contentRetrievalService = contentRetrievalService;
    }
    
       
    public ModelAndView about(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
                    
        populateCommonLocal(mv);             
        mv.addObject("heading", "About");        
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());
        
        mv.setViewName("about");                     
        return mv;
    }
    
    
    public ModelAndView archive(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);

        populateCommonLocal(mv);        
   
        mv.addObject("heading", "Archive");
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());
        
        List<ArchiveLink> archiveMonths = resourceDAO.getArchiveMonths(showBrokenDecisionService.shouldShowBroken());
        mv.addObject("archiveLinks", archiveMonths);
                
        mv.setViewName("archiveIndex");
        return mv;
    }
    
    
    public ModelAndView api(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateCommonLocal(mv);
           
        mv.addObject("heading", "The Wellynews API");

        mv.addObject("feeds", resourceDAO.getAllFeedsByName());
        mv.addObject("publishers", contentRetrievalService.getAllPublishersWithNewsitemCounts(true));	// TODO needs to include publishers with only feeds
        mv.addObject("api_tags", contentRetrievalService.getTopLevelTags());
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());        
        mv.setViewName("api");
        return mv;      
    }

    
    
    
    public ModelAndView broken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateCommonLocal(mv);
        
        mv.addObject("heading", "Broken sites");
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());
             
        List<Resource> wrappedCalendars = resourceDAO.getBrokenSites();        
        mv.addObject("main_content", wrappedCalendars);
        mv.setViewName("browse");
        return mv;
    }

    
    
        
    public ModelAndView calendars(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateCommonLocal(mv);
        
        mv.addObject("heading", "Calendar Feeds");
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());
             
        List<Resource> wrappedCalendars = resourceDAO.getAllCalendarFeeds();        
        mv.addObject("main_content", wrappedCalendars);        
        mv.setViewName("calendars");        
        return mv;
    }
    
     
       
    public ModelAndView discovered(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateCommonLocal(mv);
        
        urlStack.setUrlStack(request);

        mv.addObject("heading", "Discovered Feeds");
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());
        
        List<DiscoveredFeed> nonCommentFeeds = discoveredFeedRepository.getAllNonCommentDiscoveredFeeds();        
		mv.addObject("discovered_feeds", nonCommentFeeds);        
        mv.setViewName("discoveredFeeds");
        return mv;
    }

                
    public ModelAndView lastUpdated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateCommonLocal(mv);

        urlStack.setUrlStack(request);
      
        mv.addObject("heading", "Latest Updates");
        // TODO this should show resources order by last live time. - the the last update query won't have to interate ethier.
        mv.addObject("main_content", contentRetrievalService.getLatestNewsitems(20));
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());
        populateNewslogLastUpdated(mv);

        mv.setViewName("lastupdated");
        return mv;
    }
    
    
    

    public ModelAndView tags(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateCommonLocal(mv);
     
        mv.addObject("heading", "All Tags");        
        mv.addObject("tags", resourceDAO.getAllTags());
                
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());       
        
        mv.setViewName("tags");
        return mv;
    }
    
    
    public ModelAndView publishers(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateCommonLocal(mv);
                
        mv.addObject("heading", "All Publishers");    
        mv.addObject("publishers", contentRetrievalService.getAllPublishers(true));
        
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());       
        mv.setViewName("publishers");
        return mv;
    }
    
     
    public ModelAndView twitter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        
        mv.addObject("twitterUsername", siteInformation.getTwitterUsername());
        mv.addObject("heading",  "Following the " + siteInformation.getAreaname() + " newslog on Twitter");

        populateLatestTwitters(mv, showBrokenDecisionService.shouldShowBroken());        
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());
        
        mv.addObject("main_content", resourceDAO.getTwitterMentionedNewsitems(MAX_NEWSITEMS));
        
        populateCommonLocal(mv);
        mv.setViewName("twitter");
        return mv;
    }


	
    public ModelAndView watchlist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateCommonLocal(mv);
        
        mv.addObject("heading", "News Watchlist");
        mv.addObject("main_content", contentRetrievalService.getAllWatchlists());	// TODO limit

        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());

        setRss(mv, rssUrlBuilder.getRssTitleForWatchlist(), rssUrlBuilder.getRssUrlForWatchlist());
        mv.setViewName("watchlist");
        return mv;
    }
    
    
    private void populateLatestTwitters(ModelAndView mv, boolean showBroken) throws IOException {       
        final List<Newsitem> latestNewsitems = resourceDAO.getLatestTwitteredNewsitems(MAX_TWITTERS_TO_SHOW, showBroken);        
        mv.addObject("latest_twitters", latestNewsitems);  
    }
    
}
