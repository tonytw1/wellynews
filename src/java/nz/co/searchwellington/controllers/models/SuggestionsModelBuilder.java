 package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Suggestion;
import nz.co.searchwellington.model.SuggestionFeednewsitem;
import nz.co.searchwellington.model.TwitteredNewsitem;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionDAO;
import nz.co.searchwellington.twitter.TwitterNewsitemBuilderService;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class SuggestionsModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	private static final int MAX_SUGGESTIONS = 50;
	
	Logger log = Logger.getLogger(SuggestionsModelBuilder.class);
    	
	private ResourceRepository resourceDAO;
	private SuggestionDAO suggestionDAO;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private TwitterNewsitemBuilderService twitterNewsitemBuilder;

	
	public SuggestionsModelBuilder(ResourceRepository resourceDAO, SuggestionDAO suggestionDAO, RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder, TwitterNewsitemBuilderService twitterNewsitemBuilder) {
		this.resourceDAO = resourceDAO;
		this.suggestionDAO = suggestionDAO;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.twitterNewsitemBuilder = twitterNewsitemBuilder;
	}
	

	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/feeds/inbox(/(rss|json))?$");	
	}

	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building suggestions model");
			ModelAndView mv = new ModelAndView();
			
			List<Suggestion> bareSuggestions = suggestionDAO.getAllSuggestions();
			List<SuggestionFeednewsitem> suggestions = suggestionDAO.getSuggestionFeednewsitems(bareSuggestions, MAX_SUGGESTIONS);
			mv.addObject("main_content", suggestions); 
			
			mv.addObject("heading", "Feeds inbox");
			mv.addObject("link", urlBuilder.getFeedsInboxUrl());
			mv.addObject("description","Suggested newsitems from local feeds.");  
			
			setRss(mv, rssUrlBuilder.getTitleForSuggestions(), rssUrlBuilder.getRssUrlForFeedSuggestions());
			mv.setViewName("suggestions");
			return mv;
		}
		return null;
	}

	
	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
		List<TwitteredNewsitem> potentialTwitterSubmissions = twitterNewsitemBuilder.getPossibleSubmissions();
		mv.addObject("submissions", potentialTwitterSubmissions);
		populateSecondaryFeeds(mv);
	}
	
	
	// TODO duplication with BaseM'E'C
	public void populateSecondaryFeeds(ModelAndView mv) {      
        mv.addObject("righthand_heading", "Local Feeds");                
        mv.addObject("righthand_description", "Recently updated feeds from local organisations.");        
        final List<Feed> allFeeds = resourceDAO.getAllFeeds();
        if (allFeeds.size() > 0) {
            mv.addObject("righthand_content", allFeeds);       
        }
    }
	
}
