 package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class FeedsModelBuilder extends AbstractModelBuilder implements ModelBuilder {
	
	Logger log = Logger.getLogger(FeedsModelBuilder.class);
    	
	private ContentRetrievalService contentRetrievalService;
	
	
	public FeedsModelBuilder(ContentRetrievalService contentRetrievalService) {		
		this.contentRetrievalService = contentRetrievalService;
	}
	

	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/feeds(/(rss|json))?$");
	}

	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building feed page model");
			ModelAndView mv = new ModelAndView();
			
			mv.addObject("heading", "Feeds");        		
			mv.addObject("description", "Incoming feeds");
			mv.addObject("link", "TODO");	// TODO
			
			mv.addObject("main_content", contentRetrievalService.getAllFeeds());
			mv.setViewName("rssfeeds");
			return mv;
		}
		return null;
	}

	
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {		
	}
      
}
