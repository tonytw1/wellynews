 package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.SuggestedFeeditemsService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class FeedsModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	static Logger log = Logger.getLogger(FeedsModelBuilder.class);
    	
	private ContentRetrievalService contentRetrievalService;
	private SuggestedFeeditemsService suggestedFeeditemsService;
	private UrlBuilder urlBuilder;
		
	public FeedsModelBuilder(ContentRetrievalService contentRetrievalService, SuggestedFeeditemsService suggestedFeeditemsService, UrlBuilder urlBuilder) {		
		this.contentRetrievalService = contentRetrievalService;
		this.suggestedFeeditemsService = suggestedFeeditemsService;
		this.urlBuilder = urlBuilder;
	}
	

	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/feeds(/(rss|json))?$");
	}

	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building feed page model");
			ModelAndView mv = new ModelAndView();			
			mv.addObject("heading", "Feeds");
			mv.addObject("description", "Incoming feeds");
			mv.addObject("link", urlBuilder.getFeedsUrl());
			mv.addObject("main_content", contentRetrievalService.getAllFeeds());
			return mv;
		}
		return null;
	}

	
	@Override
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		populateSecondaryFeeds(mv);
		mv.addObject("suggestions", suggestedFeeditemsService.getSuggestionFeednewsitems(6));
		mv.addObject("discovered_feeds", contentRetrievalService.getDiscoveredFeeds());
	}
	
	
	@Override
	public String getViewName(ModelAndView mv) {
		return "feeds";
	}
	
}
