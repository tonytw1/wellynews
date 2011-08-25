package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.springframework.web.servlet.ModelAndView;

public class TwitterReactionModelBuilder extends AbstractModelBuilder implements ModelBuilder {

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
		
		final int page = getPage(request);
		mv.addObject("page", page);	// TODO push to populate pagination.
		final int startIndex = getStartIndex(page);
		final int totalTwitterReactionCount = contentRetrievalService.getTwitteredNewsitemsCount();
		if (startIndex > totalTwitterReactionCount) {
			return null;
		}
		
		populatePagination(mv, startIndex, totalTwitterReactionCount);

		mv.addObject("main_content", contentRetrievalService.getTwitteredNewsitems(startIndex, MAX_NEWSITEMS));
		mv.addObject("heading", "Following the Wellington newslog on Twitter");
		return mv;
	}

	@Override
	public void populateExtraModelConent(HttpServletRequest request, ModelAndView mv) {
	}
	
}
