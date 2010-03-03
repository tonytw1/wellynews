package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.feeds.DiscoveredFeedRepository;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;


public class SimplePageController extends BaseMultiActionController {
    
    private static final int MAX_TWITTERS_TO_SHOW = 12;

	Logger log = Logger.getLogger(SimplePageController.class);
    
    
    private SiteInformation siteInformation;
	private DiscoveredFeedRepository discoveredFeedRepository;
	private TagDAO tagDAO;
    
    
    public SimplePageController(UrlStack urlStack, ConfigRepository configDAO, 
    		SiteInformation siteInformation, DiscoveredFeedRepository discoveredFeedRepository, 
    		ContentRetrievalService contentRetrievalService, TagDAO tagDAO) {
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.siteInformation = siteInformation;        
        this.discoveredFeedRepository = discoveredFeedRepository;      
        this.contentRetrievalService = contentRetrievalService;
        this.tagDAO = tagDAO;
    }
    
       
    public ModelAndView about(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
                    
        populateCommonLocal(mv);             
        mv.addObject("heading", "About");        
        populateSecondaryLatestNewsitems(mv);
        
        mv.setViewName("about");                     
        return mv;
    }
    
    
    public ModelAndView archive(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);

        populateCommonLocal(mv);        
   
        mv.addObject("heading", "Archive");
        populateSecondaryLatestNewsitems(mv);
        
        // TODO populate stats and dedupe as well.
        List<ArchiveLink> archiveMonths = contentRetrievalService.getArchiveMonths();
        mv.addObject("archiveLinks", archiveMonths);
                
        mv.setViewName("archiveIndex");
        return mv;
    }
    
    
    public ModelAndView api(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateCommonLocal(mv);
           
        mv.addObject("heading", "The Wellynews API");

        mv.addObject("feeds", contentRetrievalService.getAllFeeds());
        mv.addObject("publishers", contentRetrievalService.getAllPublishersWithNewsitemCounts(true));	// TODO needs to include publishers with only feeds
        mv.addObject("api_tags", contentRetrievalService.getTopLevelTags());
        populateSecondaryLatestNewsitems(mv);        
        mv.setViewName("api");
        return mv;      
    }

    
    
    
    public ModelAndView broken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateCommonLocal(mv);
        
        mv.addObject("heading", "Broken sites");
        populateSecondaryLatestNewsitems(mv);
             
        List<Resource> wrappedCalendars = contentRetrievalService.getBrokenSites();        
        mv.addObject("main_content", wrappedCalendars);
        mv.setViewName("browse");
        return mv;
    }

         
    public ModelAndView discovered(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateCommonLocal(mv);
        
        urlStack.setUrlStack(request);

        mv.addObject("heading", "Discovered Feeds");
        populateSecondaryLatestNewsitems(mv);
        
        List<DiscoveredFeed> nonCommentFeeds = discoveredFeedRepository.getAllNonCommentDiscoveredFeeds();        
		mv.addObject("discovered_feeds", nonCommentFeeds);        
        mv.setViewName("discoveredFeeds");
        return mv;
    }

    
    public ModelAndView tags(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateCommonLocal(mv);
     
        mv.addObject("heading", "All Tags");        
        mv.addObject("tags", tagDAO.getAllTags());
                
        populateSecondaryLatestNewsitems(mv);       
        
        mv.setViewName("tags");
        return mv;
    }
    
    
    public ModelAndView publishers(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateCommonLocal(mv);
        
        mv.addObject("heading", "All Publishers");
        mv.addObject("publishers", contentRetrievalService.getAllPublishers());
        
        populateSecondaryLatestNewsitems(mv);       
        mv.setViewName("publishers");
        return mv;
    }
    
     
    public ModelAndView twitter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        
        mv.addObject("twitterUsername", siteInformation.getTwitterUsername());
        mv.addObject("heading",  "Following the " + siteInformation.getAreaname() + " newslog on Twitter");

        populateLatestTwitters(mv);        
        populateSecondaryLatestNewsitems(mv);
        
        mv.addObject("main_content", contentRetrievalService.getRecentedTwitteredNewsitems(MAX_NEWSITEMS));
        
        populateCommonLocal(mv);
        mv.setViewName("twitter");
        return mv;
    }
    
    private void populateLatestTwitters(ModelAndView mv) {       
        mv.addObject("latest_twitters", contentRetrievalService.getRecentedTwitteredNewsitems(MAX_TWITTERS_TO_SHOW));  
    }
    
}
