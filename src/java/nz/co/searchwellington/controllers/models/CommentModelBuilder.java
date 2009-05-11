package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.repositories.ResourceRepository;

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
		return request.getPathInfo().equals("/comment");
	}

	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building comment page model");
			
			ModelAndView mv = new ModelAndView();							
			mv.addObject("heading", "Commented");        		
			mv.addObject("description", "Commented newsitems");
			mv.addObject("link", urlBuilder.getCommentUrl());	
						
			final List<Newsitem> commentedNewsitms = resourceDAO.getAllCommentedNewsitems(500, showBroken);						
			mv.addObject("main_content", commentedNewsitms);
								
			mv.setViewName("commented");
			return mv;
		}
		return null;
	}
	

	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		mv.addObject("commented_tags", resourceDAO.getCommentedTags(showBroken)); 
	}

}
