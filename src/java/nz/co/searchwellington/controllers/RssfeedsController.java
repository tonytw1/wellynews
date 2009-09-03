package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.feeds.DiscoveredFeedRepository;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Suggestion;
import nz.co.searchwellington.model.SuggestionFeednewsitem;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionDAO;
import nz.co.searchwellington.widgets.PublisherSelectFactory;
import nz.co.searchwellington.widgets.TagWidgetFactory;

import org.apache.ecs.html.Select;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;


public class RssfeedsController extends BaseMultiActionController {

    Logger log = Logger.getLogger(RssfeedsController.class);
    
    private PublisherSelectFactory publisherSelectFactory;
    private TagWidgetFactory tagWidgetFactory;    
    private RssUrlBuilder rssUrlBuilder;
	private DiscoveredFeedRepository discoveredFeedsRepository;
	private SuggestionDAO suggestionDAO;
	private RssfeedNewsitemService rssNewsitemService;
	
    
    public RssfeedsController(ResourceRepository resourceDAO, PublisherSelectFactory publisherSelectFactory, UrlStack urlStack, ConfigRepository configDAO, 
    		TagWidgetFactory tagWidgetFactory, RssUrlBuilder rssUrlBuilder, DiscoveredFeedRepository discoveredFeedsRepository, 
    		LoggedInUserFilter loggedInUserFilter, SuggestionDAO suggestionDAO, RssfeedNewsitemService rssNewsitemService) {
        this.resourceDAO = resourceDAO;   
        this.publisherSelectFactory = publisherSelectFactory;
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.tagWidgetFactory = tagWidgetFactory;       
        this.rssUrlBuilder = rssUrlBuilder;
        this.discoveredFeedsRepository = discoveredFeedsRepository;
        this.loggedInUserFilter = loggedInUserFilter;
        this.suggestionDAO = suggestionDAO;
        this.rssNewsitemService = rssNewsitemService;
    }
    
        
    @SuppressWarnings("unchecked")
    public ModelAndView rssfeeds(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Starting rssfeeds.");
        ModelAndView mv = new ModelAndView();
        
        Website publisher = null;
        Tag tag = null;
        
        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean showBroken = loggedInUser != null;
                
        mv.getModel().put("top_level_tags", resourceDAO.getTopLevelTags());        
        
        if (request.getAttribute("publisher") != null) { 
            publisher = (Website) request.getAttribute("publisher");            
        }
        if (request.getAttribute("tag") != null) {
            tag = (Tag) request.getAttribute("tag");
        }

        
        if (publisher != null) {
            mv.getModel().put("heading", publisher.getName() + " RSS Feed");
            mv.getModel().put("custom", new Boolean(true));                        
            mv.getModel().put("main_content", resourceDAO.getPublisherNewsitems(publisher, MAX_NEWSITEMS, showBroken));
            setRss(mv, rssUrlBuilder.getRssUrlForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher));  
               
        } else if (tag != null) {
            mv.getModel().put("heading", tag.getDisplayName() + " RSS Feed");
            mv.getModel().put("custom", new Boolean(true));            
            mv.getModel().put("main_content", resourceDAO.getTaggedNewsitems(tag, showBroken, 0, MAX_NEWSITEMS));
            setRss(mv, rssUrlBuilder.getRssTitleForTag(tag), rssUrlBuilder.getRssUrlForTag(tag));        
  
        } else {
            mv.getModel().put("main_content", resourceDAO.getLatestNewsitems(MAX_NEWSITEMS, false));            
            mv.getModel().put("heading", "RSS Feeds");
            mv.getModel().put("rss_url", rssUrlBuilder.getBaseRssUrl());
            mv.getModel().put("rss_title", "Newslog");            
        }
        
        final Select tagSelect = tagWidgetFactory.createTagSelect("tag", tag, new HashSet<Tag>(), "No Tag");
        tagSelect.setOnChange("tagForm.submit();");
        mv.getModel().put("tag_select", tagSelect.toString());
        
               
        log.debug("Building publishers select.");
        final Select publisherSelect = publisherSelectFactory.createPublisherSelectWithCounts(publisher, false);        
        publisherSelect.setOnChange("publisherForm.submit();");        
        mv.getModel().put("publisher_select", publisherSelect);
        log.debug("Finished building publishers select.");
        
        populateSecondaryFeeds(mv, loggedInUser);        
        populateDiscoveredFeeds(mv);
               
        mv.addObject("suggestions", suggestionDAO.getDecoratedSuggestions(suggestionDAO.getSuggestions(6)));
        
        mv.setViewName("rssfeeds");
        log.info("Finished rssfeeds method.");
        return mv;
    }

    
    public ModelAndView suggestions(HttpServletRequest request, HttpServletResponse response) throws IOException {     
        ModelAndView mv = new ModelAndView();        
        mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());        
        mv.addObject("heading", "Feed newsitem suggestions");    
        urlStack.setUrlStack(request);
        
        List<Suggestion> bareSuggestions = suggestionDAO.getAllSuggestions();
        List<Suggestion> suggestions = suggestionDAO.getDecoratedSuggestions(bareSuggestions);        
		mv.addObject("suggestions", suggestions);       
        mv.setViewName("suggestions");    
        return mv;
    }
    
    
    private void populateDiscoveredFeeds(ModelAndView mv) {
        mv.addObject("discovered_feeds", discoveredFeedsRepository.getAllNonCommentDiscoveredFeeds());
        mv.addObject("discovered_feeds_moreurl", "feeds/discovered");
    }

    
}
    