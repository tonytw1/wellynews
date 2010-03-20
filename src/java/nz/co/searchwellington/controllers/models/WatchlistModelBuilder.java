 package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class WatchlistModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	static Logger log = Logger.getLogger(WatchlistModelBuilder.class);
    	
	private ContentRetrievalService contentRetrievalService;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	
	
	public WatchlistModelBuilder(
			ContentRetrievalService contentRetrievalService,
			RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder) {
		this.contentRetrievalService = contentRetrievalService;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
	}


	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/watchlist(/(rss|json))?$");
	}

	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building watchlist page model");
			ModelAndView mv = new ModelAndView();				
			mv.addObject("heading", "News watchlist");        		
			mv.addObject("description", "The news watchlist");
			mv.addObject("link", urlBuilder.getWatchlistUrl());
			
			mv.addObject("main_content", contentRetrievalService.getRecentlyChangedWatchlistItems());
			
			setRss(mv, rssUrlBuilder.getRssTitleForJustin(), rssUrlBuilder.getRssUrlForWatchlist());
			return mv;
		}
		return null;
	}
	
	
	@Override
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
	}

	
	@Override
	public String getViewName(ModelAndView mv) {
		return "watchlist";		
	}
	    
}
