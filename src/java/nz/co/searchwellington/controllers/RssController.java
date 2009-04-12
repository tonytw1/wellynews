package nz.co.searchwellington.controllers;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.views.RssView;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

public class RssController extends MultiActionController {

	// TODO push to config
    private static final String FEEDBURNER_RSS_URL = "http://feeds2.feedburner.com/wellynews";

	Logger log = Logger.getLogger(RssController.class);
    
    private static final int MAX_RSS_ITEMS = 30;
    private SiteInformation siteInformation;
    private RequestFilter requestFilter;
    private ResourceRepository resourceDAO;
    private RssUrlBuilder rssUrlBuilder;
	private ContentModelBuilderService contentModelBuilderService;

    
       
    public RssController(SiteInformation siteInformation, RequestFilter requestFilter, ResourceRepository resourceDAO, RssUrlBuilder rssUrlBuilder, ContentModelBuilderService contentModelBuilderService) {     
        this.siteInformation = siteInformation;
        this.requestFilter = requestFilter;
        this.resourceDAO = resourceDAO;       
        this.rssUrlBuilder = rssUrlBuilder;      
        this.contentModelBuilderService = contentModelBuilderService;
    }
    
       
    public ModelAndView mainRss(HttpServletRequest request, HttpServletResponse response) throws Exception { 
    	final String userAgent = request.getHeader("User-Agent");
		boolean clientIsFeedburner = userAgent != null && userAgent.startsWith("FeedBurner");
		if (!clientIsFeedburner) {
        	return redirectToFeedburnerMainFeed();
        }
		
		HashMap <String, Object> model = new HashMap <String, Object>();
		log.info("Building full site rss feed");
		model.put("title", "Search " + siteInformation.getAreaname() + " - " + siteInformation.getAreaname() + " Newslog");
		model.put("link", siteInformation.getUrl());
        model.put("description", "Links to " + siteInformation.getAreaname() + " related newsitems.");
        model.put("main_content", resourceDAO.getLatestNewsitems(MAX_RSS_ITEMS, false));
        
        RssView rssView = new RssView();        
        return new ModelAndView(rssView, model);        
    }
    
       
    
	public ModelAndView contentRss(HttpServletRequest request, HttpServletResponse response) throws Exception {    
    	log.info("Building content rss");
    	 requestFilter.loadAttributesOntoRequest(request);  
         ModelAndView mv = contentModelBuilderService.populateContentModel(request);
         mv.setView(new RssView());
         return mv;
    }
    
       
    public ModelAndView geotaggedRss(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	HashMap <String, Object> model = new HashMap <String, Object>();    	
    	model.put("title", rssUrlBuilder.getRssTitleForGeotagged());
    	model.put("link", siteInformation.getUrl() + "/geotagged");
    	model.put("description", "Newsitems with geotagging information.");
    	model.put("main_content", resourceDAO.getAllValidGeocoded(MAX_RSS_ITEMS, false));
    	
        RssView rssView = new RssView();
        return new ModelAndView(rssView, model);
    }
    
    
    public ModelAndView justinRss(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	HashMap <String, Object> model = new HashMap <String, Object>();     
    	model.put("title", rssUrlBuilder.getRssTitleForJustin());
    	model.put("link", siteInformation.getUrl());
    	model.put("description", "The most recently submitted website listings.");
    	model.put("main_content", resourceDAO.getLatestWebsites(MAX_RSS_ITEMS, false));
    	
        RssView rssView = new RssView();        
        return new ModelAndView(rssView, model);        
    }
    
    
    public ModelAndView watchlistRss(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	HashMap <String, Object> model = new HashMap <String, Object>();     
    	model.put("title", rssUrlBuilder.getTitleForWatchlist());
    	model.put("link", siteInformation.getUrl());
    	model.put("description","Recently updated " + siteInformation.getAreaname() + " related news pages.");          
    	model.put("main_content", resourceDAO.getRecentlyChangedWatchlistItems());
    	
        RssView rssView = new RssView();        
        return new ModelAndView(rssView, model);
        
    }
    
    
    public ModelAndView tagsRss(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	HashMap <String, Object> model = new HashMap <String, Object>();     
    	model.put("title", rssUrlBuilder.getRssTitleForJustin());
    	model.put("link", siteInformation.getUrl());
    	model.put("description", "Available tags");
    	model.put("main_content", resourceDAO.getAllTags());
    	
        RssView rssView = new RssView();        
        return new ModelAndView(rssView, model);        
    }
    
    
	private ModelAndView redirectToFeedburnerMainFeed() {
		View redirectView = new RedirectView(FEEDBURNER_RSS_URL);
		return new ModelAndView(redirectView);		
	}

}
