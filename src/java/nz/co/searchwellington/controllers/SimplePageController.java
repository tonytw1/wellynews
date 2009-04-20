package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.feeds.DiscoveredFeedRepository;
import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.twitter.TwitterService;
import nz.co.searchwellington.utils.GoogleMapsDisplayCleaner;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;


public class SimplePageController extends BaseMultiActionController {
    
    Logger log = Logger.getLogger(SimplePageController.class);
    
    final int MAX_TWITTERS_TO_SHOW = 12;
    
    private SiteInformation siteInformation;
    private RequestFilter requestFilter;
    private RssUrlBuilder rssUrlBuilder;
	private TwitterService twitterService;
	private DiscoveredFeedRepository discoveredFeedRepository;
    private LoggedInUserFilter loggedInUserFilter;
	
    public SimplePageController(ResourceRepository resourceDAO, UrlStack urlStack, ConfigRepository configDAO, SiteInformation siteInformation, RequestFilter requestFilter, RssUrlBuilder rssUrlBuilder, TwitterService twitterService, DiscoveredFeedRepository discoveredFeedRepository, LoggedInUserFilter loggedInUserFilter) {
        this.resourceDAO = resourceDAO;        
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.siteInformation = siteInformation;
        this.requestFilter = requestFilter;
        this.rssUrlBuilder = rssUrlBuilder;
        this.twitterService = twitterService;
        this.discoveredFeedRepository = discoveredFeedRepository;
        this.loggedInUserFilter = loggedInUserFilter;
    }
    
       
    public ModelAndView about(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);  
        urlStack.setUrlStack(request);
                    
        populateLocalCommon(mv);             
        mv.addObject("heading", "About");        
        populateSecondaryLatestNewsitems(mv, loggedInUserFilter.getLoggedInUser());
             
        mv.setViewName("about");                     
        return mv;
    }

    
    public ModelAndView archive(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);  
        urlStack.setUrlStack(request);

        populateLocalCommon(mv);        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        
        mv.addObject("heading", "Archive");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
            
        List<ArchiveLink> archiveMonths = resourceDAO.getArchiveMonths();
        mv.addObject("archiveLinks", archiveMonths);
                
        mv.setViewName("archiveIndex");
        return mv;
    }
    
    
    public ModelAndView calendars(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);  
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
     
        mv.addObject("heading", "Calendar Feeds");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
             
        List<Resource> wrappedCalendars = resourceDAO.getAllCalendarFeeds();        
        mv.addObject("main_content", wrappedCalendars);        
        mv.setViewName("calendars");        
        return mv;
    }
    
    
    
    public ModelAndView commented(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean showBroken = loggedInUser != null;
        
        mv.addObject("heading", "Comment");           
        mv.addObject("commented_tags", resourceDAO.getCommentedTags(showBroken));                
        mv.addObject("main_content", resourceDAO.getAllCommentedNewsitems(500, true));
        populateSecondaryLatestNewsitems(mv, loggedInUser);
        
        mv.setViewName("commented");
        return mv;      
    }

    
    
    
    public ModelAndView api(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean showBroken = loggedInUser != null;        
        mv.addObject("heading", "Wellynews API");

        List<Website> publishers = getAllPublishers(showBroken);
        
        mv.addObject("publishers", publishers);
        mv.addObject("api_tags", resourceDAO.getTopLevelTags());
        populateSecondaryLatestNewsitems(mv, loggedInUser);        
        mv.setViewName("api");
        return mv;      
    }




    
       
    public ModelAndView discovered(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        
        mv.addObject("heading", "Discovered Feeds");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
        
        List<DiscoveredFeed> nonCommentFeeds = discoveredFeedRepository.getAllNonCommentDiscoveredFeeds();        
		mv.addObject("discovered_feeds", nonCommentFeeds);        
        mv.setViewName("discoveredFeeds");
        return mv;
    }

    
    public ModelAndView geotagged(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);
        requestFilter.loadAttributesOntoRequest(request);
                
        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean showBroken = loggedInUser != null;    
        populateLocalCommon(mv);
                        
        mv.addObject("heading", "Geotagged");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
      
        mv.addObject("geotagged_tags", resourceDAO.getGeotaggedTags(showBroken));
        
        
        Resource selected = null;
		if (request.getAttribute("resource") != null) {
			selected = (Resource) request.getAttribute("resource");
            log.info("Added selected resource onto model: " + selected.getName());
        	mv.addObject("selected", selected);
        } else {
            log.info("No selected resource seen on request.");
        }
        
        populateGeocoded(mv, showBroken, selected);
        mv.setViewName("geocoded");
        return mv;
    }

    
   
    
    
    
      
    public ModelAndView justin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);
        populateLocalCommon(mv);

        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean showBroken = loggedInUser != null;
        
        mv.addObject("heading", "Latest Additions");
        mv.addObject("main_content", resourceDAO.getLatestWebsites(MAX_NEWSITEMS, showBroken));
        
        populateSecondaryLatestNewsitems(mv, loggedInUser);

        setRss(mv, rssUrlBuilder.getRssTitleForJustin(), rssUrlBuilder.getRssUrlForJustin());
        mv.setViewName("justin");
        return mv;
    }
    
        
    public ModelAndView lastUpdated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);
        populateLocalCommon(mv);

        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        
        mv.addObject("heading", "Latest Updates");
        // TODO this should show resources order by last live time. - the the last update query won't have to interate ethier.
        mv.addObject("main_content", resourceDAO.getLatestNewsitems(20, false));
        populateSecondaryLatestNewsitems(mv, loggedInUser);
        populateNewslogLastUpdated(mv);

        mv.setViewName("lastupdated");
        return mv;
    }
    
    
    

    public ModelAndView tags(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        
        mv.addObject("heading", "All Tags");        
        mv.addObject("tags", resourceDAO.getAllTags());
                
        populateSecondaryLatestNewsitems(mv, loggedInUser);
        List<Newsitem> recentNewsitems = resourceDAO.getLatestNewsitems(300, loggedInUser != null);     
        populateUsedTags(mv, loggedInUser, recentNewsitems);
        
        mv.setViewName("tags");
        return mv;
    }
    
    
    
    
    public ModelAndView publishers(HttpServletRequest request, HttpServletResponse response) throws IOException {        
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean showBroken = loggedInUser != null;
        
        mv.addObject("heading", "All Publishers");    
        mv.addObject("publishers", getAllPublishers(showBroken));
        
        populateSecondaryLatestNewsitems(mv, loggedInUser);       
        mv.setViewName("publishers");
        return mv;
    }
    
    
    
    public ModelAndView twitter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        loggedInUserFilter.loadLoggedInUser(request);
        urlStack.setUrlStack(request);
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        populateLocalCommon(mv);
        
        final String twitterUsername = siteInformation.getTwitterUsername();
        mv.addObject("twitterUsername", twitterUsername);
        mv.addObject("heading", "@" + twitterUsername + " - Following the newslog on Twitter");
        populateLatestTwitters(mv, loggedInUser);
        
        populateSecondaryLatestNewsitems(mv, loggedInUser);
        
        if(loggedInUser != null) {        	       	        	
        	mv.addObject("twitterReplies", twitterService.getReplies());        	
        }
        	
        
        mv.setViewName("twitter");
        return mv;
    }
    
    
    @SuppressWarnings("unchecked")
    private void populateLatestTwitters(ModelAndView mv, User loggedInUser) throws IOException {
        boolean showBroken = loggedInUser != null;       
        final List<Newsitem> latestNewsitems = resourceDAO.getLatestTwitteredNewsitems(MAX_TWITTERS_TO_SHOW, showBroken);        
        mv.getModel().put("latest_twitters", latestNewsitems);  
    }
    
    
    
    
   
    public ModelAndView watchlist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateLocalCommon(mv);

        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        
        mv.addObject("heading", "News Watchlist");
        mv.addObject("main_content", resourceDAO.getAllWatchlists());

        populateSecondaryLatestNewsitems(mv, loggedInUser);

        setRss(mv, rssUrlBuilder.getRssTitleForWatchlist(), rssUrlBuilder.getRssUrlForWatchlist());
        mv.setViewName("watchlist");
        return mv;
    }
    
    
    
    private void populateLocalCommon(ModelAndView mv) {
              mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());
    }
    

    // TODO extremely non preformant
	private List<Website> getAllPublishers(boolean showBroken) throws IOException {
		List<Website> publishers = new ArrayList<Website>();
        List<Object[]> publisherIds = resourceDAO.getAllPublishers(showBroken, true); 
        for (Object[] objects : publisherIds) {
			int publisherId = (Integer) objects[0];
			Website publisher = (Website) resourceDAO.loadResourceById(publisherId);
			publishers.add(publisher);			
		}
		return publishers;
	}
    
	
	
	 protected void populateGeocoded(ModelAndView mv, boolean showBroken,
			Resource selected) throws IOException {
		List<Resource> geocoded = resourceDAO.getAllValidGeocoded(50,
				showBroken);
		log.info("Found " + geocoded.size() + " valid geocoded resources.");
		if (selected != null && !geocoded.contains(selected)) {
			geocoded.add(selected);
		}

		if (geocoded.size() > 0) {
			mv.addObject("main_content", geocoded);
			// TODO inject
			GoogleMapsDisplayCleaner cleaner = new GoogleMapsDisplayCleaner();
			mv.addObject("geocoded", cleaner.dedupe(geocoded, selected));
			setRss(mv, "Geocoded newsitems RSS Feed", rssUrlBuilder
					.getRssUrlForGeotagged());
		}
	}


}
    