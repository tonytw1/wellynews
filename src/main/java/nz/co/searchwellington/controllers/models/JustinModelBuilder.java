 package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class JustinModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	private static Logger log = Logger.getLogger(JustinModelBuilder.class);
    	
	private ContentRetrievalService contentRetrievalService;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	
	@Autowired
	public JustinModelBuilder(ContentRetrievalService contentRetrievalService, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder) {		
		this.contentRetrievalService = contentRetrievalService;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
	}
	
	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/justin(/(rss|json))?$");
	}
	
	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			log.info("Building justin page model");
			ModelAndView mv = new ModelAndView();				
			mv.addObject("heading", "Latest additions");        		
			mv.addObject("description", "The most recently submitted website listings.");
			mv.addObject("link", urlBuilder.getJustinUrl());
			
			final List<FrontendResource> latestSites = contentRetrievalService.getLatestWebsites(MAX_NEWSITEMS);
			mv.addObject("main_content", latestSites);
			
			setRss(mv, rssUrlBuilder.getRssTitleForJustin(), rssUrlBuilder.getRssUrlForJustin());
			return mv;
		}
		return null;
	}
	
	@Override
	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv) {		
	}
	
	@Override
	public String getViewName(ModelAndView mv) {
		return "justin";
	}
	    
}
