package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.feeds.DiscoveredFeedRepository;
import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.widgets.PublisherSelectFactory;
import nz.co.searchwellington.widgets.TagWidgetFactory;

import org.apache.ecs.html.Select;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;


public class RssfeedsController extends BaseMultiActionController {

    Logger log = Logger.getLogger(RssfeedsController.class);

    private RequestFilter requestFilter;
    private PublisherSelectFactory publisherSelectFactory;
    private TagWidgetFactory tagWidgetFactory;    
    private SiteInformation siteInformation;
    private RssUrlBuilder rssUrlBuilder;
	private DiscoveredFeedRepository discoveredFeedsRepository;
	
    
    public RssfeedsController(ResourceRepository resourceDAO, RequestFilter requestFilter, PublisherSelectFactory publisherSelectFactory, UrlStack urlStack, ConfigRepository configDAO, TagWidgetFactory tagWidgetFactory, SiteInformation siteInformation, RssUrlBuilder rssUrlBuilder, DiscoveredFeedRepository discoveredFeedsRepository, LoggedInUserFilter loggedInUserFilter) {   
        this.resourceDAO = resourceDAO;       
        this.requestFilter = requestFilter;       
        this.publisherSelectFactory = publisherSelectFactory;
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.tagWidgetFactory = tagWidgetFactory;       
        this.siteInformation = siteInformation;
        this.rssUrlBuilder = rssUrlBuilder;
        this.discoveredFeedsRepository = discoveredFeedsRepository;
        this.loggedInUserFilter = loggedInUserFilter;
    }
    
        
    @SuppressWarnings("unchecked")
    public ModelAndView rssfeeds(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Starting rssfeeds.");
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);
        requestFilter.loadAttributesOntoRequest(request);
        
        Website publisher = null;
        Tag tag = null;
        
        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean showBroken = loggedInUser != null;
        
        
        mv.getModel().put("top_level_tags", resourceDAO.getTopLevelTags());        
        mv.getModel().put("description", "Local newsitems and RSS feeds from " + siteInformation.getAreaname() + " based organisations.");

        
        
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
            mv.getModel().put("rss_url", siteInformation.getUrl() + "/rss");
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
        
        mv.setViewName("rssfeeds");
        log.info("Finished rssfeeds method.");
        return mv;
    }


    private void populateDiscoveredFeeds(ModelAndView mv) {
        mv.addObject("discovered_feeds", discoveredFeedsRepository.getAllNonCommentDiscoveredFeeds());
        mv.addObject("discovered_feeds_moreurl", "feeds/discovered");
    }

}
    