package nz.co.searchwellington.controllers.models;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService;
import nz.co.searchwellington.widgets.TagWidgetFactory;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class NewsitemPageModelBuilder implements ModelBuilder {
	
	static Logger logger = Logger.getLogger(NewsitemPageModelBuilder.class);

	private ContentRetrievalService contentRetrievalService;
	private TaggingReturnsOfficerService taggingReturnsOfficerService;
	private TagWidgetFactory tagWidgetFactory;
	private HandTaggingDAO tagVoteDAO;
	private LoggedInUserFilter loggedInUserFilter;
	
	public NewsitemPageModelBuilder(ContentRetrievalService contentRetrievalService, TaggingReturnsOfficerService taggingReturnsOfficerService, 
			TagWidgetFactory tagWidgetFactory, HandTaggingDAO tagVoteDAO, LoggedInUserFilter loggedInUserFilter) {
		this.contentRetrievalService = contentRetrievalService;
		this.taggingReturnsOfficerService = taggingReturnsOfficerService;
		this.tagWidgetFactory = tagWidgetFactory;
		this.tagVoteDAO = tagVoteDAO;
		this.loggedInUserFilter = loggedInUserFilter;
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
			mv.addObject("heading", newsitem.getName());
            mv.addObject("votes", taggingReturnsOfficerService.complieTaggingVotes(newsitem));
            mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUserFilter.getLoggedInUser(), newsitem)));
			return mv;
		}
		return null;
	}

	
	@Override
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {        
	}

	
}
