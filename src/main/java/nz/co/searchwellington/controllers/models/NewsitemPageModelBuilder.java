package nz.co.searchwellington.controllers.models;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService;
import nz.co.searchwellington.widgets.TagsWidgetFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class NewsitemPageModelBuilder implements ModelBuilder {
	
	private static Logger logger = Logger.getLogger(NewsitemPageModelBuilder.class);

	private ContentRetrievalService contentRetrievalService;
	private TaggingReturnsOfficerService taggingReturnsOfficerService;
	private TagsWidgetFactory tagWidgetFactory;
	private HandTaggingDAO tagVoteDAO;
	private LoggedInUserFilter loggedInUserFilter;
	private HibernateResourceDAO resourceDAO;
	
	public NewsitemPageModelBuilder() {
	}
	
	@Autowired
	public NewsitemPageModelBuilder(ContentRetrievalService contentRetrievalService, TaggingReturnsOfficerService taggingReturnsOfficerService, 
			TagsWidgetFactory tagWidgetFactory, HandTaggingDAO tagVoteDAO, LoggedInUserFilter loggedInUserFilter, HibernateResourceDAO resourceDAO) {
		this.contentRetrievalService = contentRetrievalService;
		this.taggingReturnsOfficerService = taggingReturnsOfficerService;
		this.tagWidgetFactory = tagWidgetFactory;
		this.tagVoteDAO = tagVoteDAO;
		this.loggedInUserFilter = loggedInUserFilter;
		this.resourceDAO = resourceDAO;
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
	public ModelAndView populateContentModel(HttpServletRequest request) {
		logger.info("Retrieving newsitem for path: " + request.getPathInfo());
		ModelAndView mv = new ModelAndView();				
		final FrontendResource frontendResource = contentRetrievalService.getNewsPage(request.getPathInfo());
		if (frontendResource != null) {
			logger.info("Putting newsitem onto page: " + frontendResource.getName());
			mv.addObject("item", frontendResource);
			mv.addObject("heading", frontendResource.getName());
			if (frontendResource.getPlace() != null) {
				mv.addObject("geocoded", Arrays.asList(frontendResource));
			}
			
			final Resource resource = resourceDAO.loadResourceById(frontendResource.getId());	// TODO Caused by model confusion Null safe
			mv.addObject("votes", taggingReturnsOfficerService.complieTaggingVotes(resource));
			mv.addObject("geotag_votes", taggingReturnsOfficerService.getGeotagVotesForResource(resource));            
            mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUserFilter.getLoggedInUser(), resource)));
			return mv;
		}
		return null;
	}
	
	@Override
	public void populateExtraModelContent(HttpServletRequest request, ModelAndView mv) {
		mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5));
	}
	
}
