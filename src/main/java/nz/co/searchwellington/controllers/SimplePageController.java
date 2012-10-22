package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.annotations.Timed;
import nz.co.searchwellington.feeds.DiscoveredFeedRepository;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.repositories.ConfigDAO;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.TagDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SimplePageController {
	
	private DiscoveredFeedRepository discoveredFeedRepository;
	private TagDAO tagDAO;
	private RssUrlBuilder rssUrlBuilder;
	private CommonModelObjectsService commonModelObjectsService;
	private UrlStack urlStack;
	private ContentRetrievalService contentRetrievalService;
	
	public SimplePageController() {
	}	
	
	@Autowired
	public SimplePageController(
			DiscoveredFeedRepository discoveredFeedRepository, TagDAO tagDAO,
			RssUrlBuilder rssUrlBuilder,
			CommonModelObjectsService commonModelObjectsService,
			UrlStack urlStack, ConfigDAO configDAO,
			ContentRetrievalService contentRetrievalService) {
		this.discoveredFeedRepository = discoveredFeedRepository;
		this.tagDAO = tagDAO;
		this.rssUrlBuilder = rssUrlBuilder;
		this.commonModelObjectsService = commonModelObjectsService;
		this.urlStack = urlStack;
		this.contentRetrievalService = contentRetrievalService;
	}
	
	@RequestMapping("/about")
    @Timed(timingNotes = "")
    public ModelAndView about(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
                    
        commonModelObjectsService.populateCommonLocal(mv);             
        mv.addObject("heading", "About");        
        mv.setViewName("about");
        mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5));
        return mv;
    }
        
    @RequestMapping("/archive")
    public ModelAndView archive(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        commonModelObjectsService.populateCommonLocal(mv);        
   
        mv.addObject("heading", "Archive");        
        // TODO populate stats and dedupe as well.
        List<ArchiveLink> archiveMonths = contentRetrievalService.getArchiveMonths();
        mv.addObject("archiveLinks", archiveMonths);
                
        mv.setViewName("archiveIndex");
        return mv;
    }
    
    @RequestMapping("/api")
    public ModelAndView api(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        commonModelObjectsService.populateCommonLocal(mv);
           
        mv.addObject("heading", "The Wellynews API");

        mv.addObject("feeds", contentRetrievalService.getAllFeeds());
        mv.addObject("publishers", contentRetrievalService.getAllPublishers());
        mv.addObject("api_tags", contentRetrievalService.getTopLevelTags());
        mv.setViewName("api");
        return mv;
    }
    
    @RequestMapping("/rssfeeds")
    public ModelAndView rssfeeds(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        commonModelObjectsService.populateCommonLocal(mv);
           
        mv.addObject("heading", "RSS feeds");
        setRss(mv, rssUrlBuilder.getBaseRssTitle(), rssUrlBuilder.getBaseRssUrl());

        mv.addObject("feedable_tags", contentRetrievalService.getFeedworthyTags());		
        mv.setViewName("rssfeeds");
        return mv;      
    }
    
    // TODO duplicated from AMB
    protected void setRss(ModelAndView mv, String title, String url) {
		mv.addObject("rss_title", title);
		mv.addObject("rss_url", url);
	}
    
    @RequestMapping("/broken")
    public ModelAndView broken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        commonModelObjectsService.populateCommonLocal(mv);
        
        mv.addObject("heading", "Broken sites");             
        mv.addObject("main_content", contentRetrievalService.getBrokenSites());
        mv.setViewName("browse");
        return mv;
    }
    
    @RequestMapping("/feeds/discovered")
    public ModelAndView discovered(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        commonModelObjectsService.populateCommonLocal(mv);
        
        urlStack.setUrlStack(request);

        mv.addObject("heading", "Discovered Feeds");        
        List<DiscoveredFeed> nonCommentFeeds = discoveredFeedRepository.getAllNonCommentDiscoveredFeeds();        
		mv.addObject("discovered_feeds", nonCommentFeeds);        
        mv.setViewName("discoveredFeeds");
        return mv;
    }

    @RequestMapping("/tags")
    public ModelAndView tags(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        commonModelObjectsService.populateCommonLocal(mv);
     
        mv.addObject("heading", "All Tags");        
        mv.addObject("tags", tagDAO.getAllTags());        
        mv.setViewName("tags");
        return mv;
    }
    
    @RequestMapping("/publishers")
    public ModelAndView publishers(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        commonModelObjectsService.populateCommonLocal(mv);
        
        mv.addObject("heading", "All Publishers");
        mv.addObject("publishers", contentRetrievalService.getAllPublishers());
        mv.setViewName("publishers");
        mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5));
        return mv;
    }
    
    @RequestMapping("/signin")
    public ModelAndView signin(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();
        commonModelObjectsService.populateCommonLocal(mv);        
        mv.addObject("heading", "Sign in");
        mv.setViewName("signin");
        return mv;
    }
    
}
