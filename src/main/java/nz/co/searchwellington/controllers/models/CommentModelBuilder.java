package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class CommentModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	private static Logger log = Logger.getLogger(CommentModelBuilder.class);
	
	private ContentRetrievalService contentRetrievalService;
	private UrlBuilder urlBuilder;
		
	@Autowired
	public CommentModelBuilder(ContentRetrievalService contentRetrievalService, UrlBuilder urlBuilder) {		
		this.contentRetrievalService = contentRetrievalService;
		this.urlBuilder = urlBuilder;
	}
	
	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/comment(/(rss|json))?$");
	}
	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request) {
		if (isValid(request)) {
			log.info("Building comment page model");
			
			ModelAndView mv = new ModelAndView();							
			mv.addObject("heading", "Commented newsitems");        		
			mv.addObject("description", "Commented newsitems");
			mv.addObject("link", urlBuilder.getCommentUrl());	
						
			int page = getPage(request);
			int startIndex = getStartIndex(page);
			final List<FrontendResource> commentedNewsitms = contentRetrievalService.getCommentedNewsitems(MAX_NEWSITEMS, startIndex);
			mv.addObject("main_content", commentedNewsitms);
			
			int commentedCounted = contentRetrievalService.getCommentedNewsitemsCount();
			mv.addObject("main_content_total", commentedCounted);	
			return mv;
		}
		return null;
	}
	
	@Override
	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv) {
		mv.addObject("commented_tags", contentRetrievalService.getCommentedTags()); 
	}
	
	@Override
	public String getViewName(ModelAndView mv) {
		return "commented";
	}

}
