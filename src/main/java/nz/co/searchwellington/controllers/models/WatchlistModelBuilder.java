 package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class WatchlistModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	private static Logger log = Logger.getLogger(WatchlistModelBuilder.class);
    	
	private ContentRetrievalService contentRetrievalService;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	
	@Autowired
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
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			log.debug("Building watchlist page model");
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
	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv) {
	}

	@Override
	public String getViewName(ModelAndView mv) {
		return "watchlist";		
	}
	    
}
