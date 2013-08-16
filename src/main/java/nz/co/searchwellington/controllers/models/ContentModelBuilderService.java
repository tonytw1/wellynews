package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.CommonModelObjectsService;
import nz.co.searchwellington.views.RssViewFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.common.views.json.JsonView;

@Component
public class ContentModelBuilderService {
	
	private static Logger logger = Logger.getLogger(ContentModelBuilderService.class);

	private static final String JSON_CALLBACK_PARAMETER = "callback";
	
	private RssViewFactory rssViewFactory;
	private ViewFactory viewFactory;
	private JsonCallbackNameValidator jsonCallbackNameValidator;
	private CommonModelObjectsService commonModelObjectsService;
	private ModelBuilder[] modelBuilders;
	
	public ContentModelBuilderService() {
	}

	@Autowired
	public ContentModelBuilderService(RssViewFactory rssViewFactory,
			ViewFactory viewFactory,
			JsonCallbackNameValidator jsonCallbackNameValidator,
			CommonModelObjectsService commonModelObjectsService,
			ModelBuilder[] modelBuilders) {
		this.rssViewFactory = rssViewFactory;
		this.viewFactory = viewFactory;
		this.jsonCallbackNameValidator = jsonCallbackNameValidator;
		this.commonModelObjectsService = commonModelObjectsService;
		this.modelBuilders = modelBuilders;
	}
	
	public ModelAndView populateContentModel(HttpServletRequest request) {
		for (int i = 0; i < modelBuilders.length; i++) {
			ModelBuilder modelBuilder = modelBuilders[i];
			if (modelBuilder.isValid(request)) {
				logger.info("Using " + modelBuilder.getClass().getName() + " to serve path: " + request.getPathInfo());
				
				ModelAndView mv = modelBuilder.populateContentModel(request);
				
				final String path = request.getPathInfo();
				if (path.endsWith("/rss")) {
					logger.info("Selecting rss view for path: " + path);
					mv.setView(rssViewFactory.makeView());
					return mv;
				}				
				if (path.endsWith("/json")) {
					logger.info("Selecting json view for path: " + path);
					final JsonView jsonView = viewFactory.getJsonView();
					jsonView.setDataField("main_content");
					mv.setView(jsonView);					
					populateJsonCallback(request, mv);								
					return mv;
				}
				
				if (mv != null) {
					modelBuilder.populateExtraModelContent(request, mv);
					mv.setViewName(modelBuilder.getViewName(mv));
					commonModelObjectsService.populateCommonLocal(mv);
					return mv;
				}
				return null;				
			}
		}
		logger.warn("No matching model builders found for path: " + request.getPathInfo());
        return null;
	}

	private void populateJsonCallback(HttpServletRequest request, ModelAndView mv) {
		if(request.getParameter(JSON_CALLBACK_PARAMETER) != null) {
			final String callback = request.getParameter(JSON_CALLBACK_PARAMETER);
			if (jsonCallbackNameValidator.isValidCallbackName(callback)) {
				logger.info("Adding callback to model:" + callback);
				mv.addObject(JSON_CALLBACK_PARAMETER, callback);
			}	 
		}
	}
	
}
