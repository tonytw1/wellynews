package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class CommentModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	Logger log = Logger.getLogger(CommentModelBuilder.class);
	
	private ResourceRepository resourceDAO;
	private UrlBuilder urlBuilder;
		
	public CommentModelBuilder(ResourceRepository resourceDAO, UrlBuilder urlBuilder) {
		this.resourceDAO = resourceDAO;
		this.urlBuilder = urlBuilder;
	}

	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/comment(/(rss|json))?$");
	}

	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building comment page model");
			
			ModelAndView mv = new ModelAndView();							
			mv.addObject("heading", "Commented newsitems");        		
			mv.addObject("description", "Commented newsitems");
			mv.addObject("link", urlBuilder.getCommentUrl());	
						
			int page = getPage(request);
			int startIndex = getStartIndex(page);
			final List<Resource> commentedNewsitms = resourceDAO.getCommentedNewsitems(MAX_NEWSITEMS, showBroken, true, startIndex);
			mv.addObject("main_content", commentedNewsitms);
			
			int commentedCounted = resourceDAO.getCommentedNewsitemsCount(showBroken);
			mv.addObject("main_content_total", commentedCounted);
			
			mv.setViewName("commented");
			return mv;
		}
		return null;
	}
	

	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		mv.addObject("commented_tags", resourceDAO.getCommentedTags(showBroken)); 
	}

}
