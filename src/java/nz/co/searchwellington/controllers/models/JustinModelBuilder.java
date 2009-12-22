 package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class JustinModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	Logger log = Logger.getLogger(JustinModelBuilder.class);
    	
	private ResourceRepository resourceDAO;	
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	
 
	public JustinModelBuilder(ResourceRepository resourceDAO, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder) {
		this.resourceDAO = resourceDAO;	
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
	}

	
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/justin(/(rss|json))?$");
	}

	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building justin page model");
			ModelAndView mv = new ModelAndView();				
			mv.addObject("heading", "Latest additions");        		
			mv.addObject("description", "The most recently submitted website listings.");
			mv.addObject("link", urlBuilder.getJustinUrl());
			
			final List<Resource> latestSites = resourceDAO.getLatestWebsites(MAX_NEWSITEMS, showBroken);
			mv.addObject("main_content", latestSites);
			
			setRss(mv, rssUrlBuilder.getRssTitleForJustin(), rssUrlBuilder.getRssUrlForJustin());
			mv.setViewName("justin");
			return mv;
		}
		return null;
	}


	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {		
	}

	    
}
