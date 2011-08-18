package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.springframework.web.servlet.ModelAndView;

public class TwitterReactionModelBuilder implements ModelBuilder {

	private ContentRetrievalService contentRetrievalService;
	
	public TwitterReactionModelBuilder(ContentRetrievalService contentRetrievalService) {
		this.contentRetrievalService = contentRetrievalService;
	}

	@Override
	public String getViewName(ModelAndView mv) {
		return "twitter";
	}

	@Override
	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo() != null && request.getPathInfo().equals("/twitter");
	}
	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
		mv.addObject("main_content", contentRetrievalService.getRecentedTwitteredNewsitems());
		mv.addObject("heading", "Following the Wellington newslog on Twitter");
		return mv;
	}

	@Override
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		// TODO Auto-generated method stub
		
	}
	
	

}
