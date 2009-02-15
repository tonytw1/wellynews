package nz.co.searchwellington.controllers;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.views.RssView;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

public class RssController extends AbstractController {

	// TODO move this to the config object.
    private static final int MAX_RSS_ITEMS = 30;
    private SiteInformation siteInformation;
    private RequestFilter requestFilter;
    private ResourceRepository resourceDAO;
    private ConfigRepository configDAO;
    private RssUrlBuilder rssUrlBuilder;

    
    
    
    
    public RssController(SiteInformation siteInformation, RequestFilter requestFilter, ResourceRepository resourceDAO, ConfigRepository configDAO, RssUrlBuilder rssUrlBuilder) {
        super();
        this.siteInformation = siteInformation;
        this.requestFilter = requestFilter;
        this.resourceDAO = resourceDAO;
        this.configDAO = configDAO;
        this.rssUrlBuilder = rssUrlBuilder;
    }

    
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String type = null;
        Website publisher = null;
        Tag tag = null;

        requestFilter.loadAttributesOntoRequest(request);
        
        boolean isMainRssFeed = request.getPathInfo().equals("/rss") || request.getPathInfo().equals("/rss/");
        final String userAgent = request.getHeader("User-Agent");
		boolean clientIsFeedburner = userAgent != null && userAgent.startsWith("FeedBurner");
		if (isMainRssFeed && !clientIsFeedburner) {
        	return redirectToFeedburnerMainFeed();
        }
        

        type = request.getParameter("type");
        if (type == null) {
            // Try to extract type from url.
            type = requestFilter.getRssTypeFromRequest(request.getPathInfo());                    
        }
        
        
        if (request.getAttribute("publisher") != null) {
            publisher = (Website) request.getAttribute("publisher");
        } 
        if (request.getAttribute("tag") != null) {
            tag = (Tag) request.getAttribute("tag");
        }
        

        HashMap <String, Object> model = new HashMap <String, Object>();
        
        boolean isGeotaggedFeed = request.getPathInfo().startsWith("/rss/geotagged");            
        if (isGeotaggedFeed) {
            model.put("title", rssUrlBuilder.getRssTitleForGeotagged());
            model.put("link", siteInformation.getUrl() + "/geotagged");
            model.put("description", "Newsitems with geotagging information.");
            model.put("main_content", resourceDAO.getAllValidGeocoded(MAX_RSS_ITEMS, false));
            
        } else if (type != null && type.equals("W")) {    
            model.put("title", rssUrlBuilder.getRssTitleForJustin());
            model.put("link", siteInformation.getUrl());
            model.put("description", "The most recently submitted website listings.");
            model.put("main_content", resourceDAO.getLatestWebsites(MAX_RSS_ITEMS, false));
            
        } else if (type != null && type.equals("L")) {      
            model.put("title", rssUrlBuilder.getTitleForWatchlist());
            model.put("link", siteInformation.getUrl());
            model.put("description","Recently updated " + siteInformation.getAreaname() + " related news pages.");          
            model.put("main_content", resourceDAO.getRecentlyChangedWatchlistItems());
                        
        } else if (publisher != null) {
            model.put("title", rssUrlBuilder.getRssTitleForPublisher(publisher));
            model.put("link", publisher.getUrl());
            model.put("description", "Newsitems published by " + publisher.getName());
            model.put("main_content", resourceDAO.getPublisherNewsitems(publisher, MAX_RSS_ITEMS, false));
            
        } else if (tag != null) {            
            model.put("title", rssUrlBuilder.getRssTitleForTag(tag));
            // TODO do we have a UrlBuilder to make these? - should have
            model.put("link", siteInformation.getUrl() + "/tag/" + tag.getName());
            model.put("description", siteInformation.getAreaname() + " related newsitems tagged as " + tag.getDisplayName());
            model.put("main_content", resourceDAO.getTaggedNewitems(tag, false, MAX_RSS_ITEMS)); 
        
        } else {
            model.put("title", "Search " + siteInformation.getAreaname() + " - " + siteInformation.getAreaname() + " Newslog");
            model.put("link", siteInformation.getUrl());
            model.put("description", "Links to " + siteInformation.getAreaname() + " related newsitems.");
            model.put("main_content", resourceDAO.getLatestNewsitems(MAX_RSS_ITEMS, false));
        }

        RssView rssView = new RssView();
        if (configDAO.getUseClickThroughCounter()) {
            rssView.setClickThroughUrl(siteInformation.getUrl() + "/clickthrough");
        }
        
        return new ModelAndView(rssView, model);
    }


	private ModelAndView redirectToFeedburnerMainFeed() {
		View redirectView = new RedirectView("http://feeds2.feedburner.com/wellynews");
		return new ModelAndView(redirectView);
		
	}

}
