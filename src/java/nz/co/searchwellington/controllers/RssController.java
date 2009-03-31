package nz.co.searchwellington.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ConfigRepository;
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
    private ConfigRepository configDAO;
    private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;

    
       
    public RssController(SiteInformation siteInformation, RequestFilter requestFilter, ResourceRepository resourceDAO, ConfigRepository configDAO, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder) {     
        this.siteInformation = siteInformation;
        this.requestFilter = requestFilter;
        this.resourceDAO = resourceDAO;
        this.configDAO = configDAO;
        this.rssUrlBuilder = rssUrlBuilder;
        this.urlBuilder = urlBuilder;
    }
    
    @SuppressWarnings("unchecked")
	public ModelAndView rss(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
        Website publisher = null;
        Tag tag = null;
        List<Tag> tags = new ArrayList<Tag>();

        requestFilter.loadAttributesOntoRequest(request);        
        if (request.getAttribute("publisher") != null) {
            publisher = (Website) request.getAttribute("publisher");
        }
        if (request.getAttribute("tag") != null) {
            tag = (Tag) request.getAttribute("tag");
        }
        
        if (request.getAttribute("tags") != null) {
        	tags = (List<Tag>) request.getAttribute("tags");
        }
 
        HashMap <String, Object> model = new HashMap <String, Object>();           
        if (publisher != null) {
            model.put("title", rssUrlBuilder.getRssTitleForPublisher(publisher));
            model.put("link", publisher.getUrl());
            model.put("description", "Newsitems published by " + publisher.getName());
            model.put("main_content", resourceDAO.getPublisherNewsitems(publisher, MAX_RSS_ITEMS, false));
            
        } else if (tags.size() == 2) {
        	log.info("Building combiner rss feed");
        	Tag firstTag = tags.get(0);
			Tag secondTag = tags.get(1);
			model.put("link", urlBuilder.getTagCombinerUrl(firstTag, secondTag));
        	model.put("title", rssUrlBuilder.getRssTitleForTagCombiner(firstTag, secondTag));
        	model.put("description", siteInformation.getAreaname() + " related newsitems tagged with " + firstTag.getDisplayName() + " and " + secondTag.getDisplayName());
        	model.put("main_content", resourceDAO.getTaggedNewsitems(new HashSet<Tag>(tags), false, MAX_RSS_ITEMS));
        	
        } else if (tag != null) {            
            model.put("title", rssUrlBuilder.getRssTitleForTag(tag));        
            model.put("link", urlBuilder.getTagUrl(tag));
            model.put("description", siteInformation.getAreaname() + " related newsitems tagged as " + tag.getDisplayName());
            model.put("main_content", resourceDAO.getTaggedNewitems(tag, false, MAX_RSS_ITEMS));         
        } 

        RssView rssView = new RssView();
        if (configDAO.getUseClickThroughCounter()) {
            rssView.setClickThroughUrl(siteInformation.getUrl() + "/clickthrough");
        }
        
        return new ModelAndView(rssView, model);
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
    

	private ModelAndView redirectToFeedburnerMainFeed() {
		View redirectView = new RedirectView(FEEDBURNER_RSS_URL);
		return new ModelAndView(redirectView);		
	}

}
