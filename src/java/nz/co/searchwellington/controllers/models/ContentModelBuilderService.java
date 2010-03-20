package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.views.RssViewFactory;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class ContentModelBuilderService {

	static Logger logger = Logger.getLogger(ContentModelBuilderService.class);
		
	private LoggedInUserFilter loggedInUserFilter;
	private RssViewFactory rssViewFactory;
	private ModelBuilder[] modelBuilders;
	
	
	public ContentModelBuilderService(LoggedInUserFilter loggedInUserFilter,
			RssViewFactory rssViewFactory, ModelBuilder[] modelBuilders) {
		this.loggedInUserFilter = loggedInUserFilter;
		this.rssViewFactory = rssViewFactory;
		this.modelBuilders = modelBuilders;
	}


	public ModelAndView populateContentModel(HttpServletRequest request) {
		logger.info("Building content model");
		boolean showBroken = false;	
		if (loggedInUserFilter.getLoggedInUser() != null) {
			showBroken = true;
		}
		for (int i = 0; i < modelBuilders.length; i++) {
			ModelBuilder modelBuilder = modelBuilders[i];
			if (modelBuilder.isValid(request)) {
				logger.debug("Using " + modelBuilder);
				ModelAndView mv = modelBuilder.populateContentModel(request, showBroken);
				
				final String path = request.getPathInfo();
				if (path.endsWith("/rss")) {
					logger.info("Selecting rss view for path: " + path);
					mv.setView(rssViewFactory.makeView());
					return mv;
				}
				
				modelBuilder.populateExtraModelConent(request, showBroken, mv);
				mv.setViewName(modelBuilder.getViewName(mv));
				return mv;
			}
		}
		logger.info("No matching model builders found for path: " + request.getPathInfo());
        return null;
	}
	
}
