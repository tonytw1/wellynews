package nz.co.searchwellington.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import com.sun.syndication.io.FeedException;

public class TagController extends MultiActionController {
	
    private static Logger log = Logger.getLogger(TagController.class);
    
    private ContentModelBuilderService contentModelBuilder;
    private UrlStack urlStack;
    private ContentRetrievalService contentRetrievalService;
    
	public TagController(ContentModelBuilderService contentModelBuilder, UrlStack urlStack, ContentRetrievalService contentRetrievalService) {
		this.contentModelBuilder = contentModelBuilder;
		this.urlStack = urlStack;
		this.contentRetrievalService = contentRetrievalService;
	}

	public ModelAndView normal(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        log.info("Starting normal content");        
		ModelAndView mv = contentModelBuilder.populateContentModel(request);
		if (mv != null) {			
			boolean isHtmlView = isHtmlView(mv);			
			if (isHtmlView) {
				urlStack.setUrlStack(request);
				addCommonModelElements(mv);
			}			
			return mv;
		}
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;
    }
	
	private boolean isHtmlView(ModelAndView mv) {
		return mv.getViewName() != null;
	}
		
	private void addCommonModelElements(ModelAndView mv) throws IOException {
		mv.addObject("top_level_tags", contentRetrievalService.getTopLevelTags());
		mv.addObject("featuredTags", contentRetrievalService.getFeaturedTags());
	}
	
}
