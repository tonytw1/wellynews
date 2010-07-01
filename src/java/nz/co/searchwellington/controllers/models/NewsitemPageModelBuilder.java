package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class NewsitemPageModelBuilder implements ModelBuilder {
	
	static Logger logger = Logger.getLogger(NewsitemPageModelBuilder.class);

	
	private ContentRetrievalService contentRetrievalService;
	

	public NewsitemPageModelBuilder(ContentRetrievalService contentRetrievalService) {
		this.contentRetrievalService = contentRetrievalService;
	}
	

	@Override
	public String getViewName(ModelAndView mv) {
		return "newsitemPage";		
	}

	
	@Override
	public boolean isValid(HttpServletRequest request) {
		logger.info("Checking valid: " + request.getPathInfo());
		return request.getPathInfo().matches("^/.*?/\\d\\d\\d\\d/[a-z]{3}/\\d\\d?/.*?$");
	}
	
	
	@Override
	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		logger.info("Retrieving newsitem for path: " + request.getPathInfo());
		ModelAndView mv = new ModelAndView();				
		Newsitem newsitem = contentRetrievalService.getNewsPage(request.getPathInfo());
		if (newsitem != null) {
			logger.info("Putting newsitem onto page: " + newsitem.getName());
			mv.addObject("item", newsitem);
			return mv;
		}
		return null;
	}

	
	@Override
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
	}

	
}
