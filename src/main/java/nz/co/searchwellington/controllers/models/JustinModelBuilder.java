 package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class JustinModelBuilder implements ModelBuilder {
	
	private static Logger log = Logger.getLogger(JustinModelBuilder.class);
    	
	private ContentRetrievalService contentRetrievalService;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
    private CommonAttributesModelBuilder commonAttributesModelBuilder;
	
	@Autowired
	public JustinModelBuilder(ContentRetrievalService contentRetrievalService, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder, CommonAttributesModelBuilder commonAttributesModelBuilder) {
		this.contentRetrievalService = contentRetrievalService;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
        this.commonAttributesModelBuilder = commonAttributesModelBuilder;
	}
	
	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/justin(/(rss|json))?$");
	}
	
	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			log.debug("Building justin page model");
			ModelAndView mv = new ModelAndView();				
			mv.addObject("heading", "Latest additions");        		
			mv.addObject("description", "The most recently submitted website listings.");
			mv.addObject("link", urlBuilder.getJustinUrl());
			
			final List<FrontendResource> latestSites = contentRetrievalService.getLatestWebsites(CommonAttributesModelBuilder.MAX_NEWSITEMS);
			mv.addObject("main_content", latestSites);
			
			commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getRssTitleForJustin(), rssUrlBuilder.getRssUrlForJustin());
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
