 package nz.co.searchwellington.controllers.models;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.TwitteredNewsitem;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.SuggestedFeeditemsService;
import nz.co.searchwellington.twitter.TwitterNewsitemBuilderService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class SuggestionsModelBuilder extends AbstractModelBuilder implements ModelBuilder {

	private static final int MAX_SUGGESTIONS = 50;
	
	Logger log = Logger.getLogger(SuggestionsModelBuilder.class);
    	
	private SuggestedFeeditemsService suggestedFeeditemsService;
	private RssUrlBuilder rssUrlBuilder;
	private UrlBuilder urlBuilder;
	private TwitterNewsitemBuilderService twitterNewsitemBuilder;
	private ContentRetrievalService contentRetrievalService;

	
	public SuggestionsModelBuilder(
			SuggestedFeeditemsService suggestedFeeditemsService,
			RssUrlBuilder rssUrlBuilder, UrlBuilder urlBuilder,
			TwitterNewsitemBuilderService twitterNewsitemBuilder,
			ContentRetrievalService contentRetrievalService) {
		this.suggestedFeeditemsService = suggestedFeeditemsService;
		this.rssUrlBuilder = rssUrlBuilder;
		this.urlBuilder = urlBuilder;
		this.twitterNewsitemBuilder = twitterNewsitemBuilder;
		this.contentRetrievalService = contentRetrievalService;
	}
	

	public boolean isValid(HttpServletRequest request) {
		return request.getPathInfo().matches("^/feeds/inbox(/(rss|json))?$");	
	}

	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
		if (isValid(request)) {
			log.info("Building feeds inbox model");
			ModelAndView mv = new ModelAndView();
			
			mv.addObject("main_content", suggestedFeeditemsService.getSuggestionFeednewsitems(MAX_SUGGESTIONS)); 
			
			mv.addObject("heading", "Inbox");
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
        if (contentRetrievalService.getAllFeeds().size() > 0) {
            mv.addObject("righthand_content", contentRetrievalService.getAllFeeds());       
        }
    }
	
}
