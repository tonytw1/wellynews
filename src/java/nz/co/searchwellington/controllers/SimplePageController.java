package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.unto.twitter.TwitterProtos.Status;
import nz.co.searchwellington.feeds.DiscoveredFeedRepository;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.TwitteredNewsitem;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.twitter.TwitterNewsitemBuilderService;
import nz.co.searchwellington.twitter.TwitterService;
import nz.co.searchwellington.utils.GoogleMapsDisplayCleaner;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;


public class SimplePageController extends BaseMultiActionController {
    
    Logger log = Logger.getLogger(SimplePageController.class);
    
    final int MAX_TWITTERS_TO_SHOW = 12;
    
    private SiteInformation siteInformation;
    private RssUrlBuilder rssUrlBuilder;
	private TwitterService twitterService;
	private DiscoveredFeedRepository discoveredFeedRepository;
    private LoggedInUserFilter loggedInUserFilter;
    private TwitterNewsitemBuilderService twitterNewsitemsService;
	
    public SimplePageController(ResourceRepository resourceDAO, UrlStack urlStack, ConfigRepository configDAO, SiteInformation siteInformation, RssUrlBuilder rssUrlBuilder, TwitterService twitterService, DiscoveredFeedRepository discoveredFeedRepository, LoggedInUserFilter loggedInUserFilter, TwitterNewsitemBuilderService newsitemBuilder) {
        this.resourceDAO = resourceDAO;        
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.siteInformation = siteInformation;        
        this.rssUrlBuilder = rssUrlBuilder;
        this.twitterService = twitterService;
        this.discoveredFeedRepository = discoveredFeedRepository;
        this.loggedInUserFilter = loggedInUserFilter;
        this.twitterNewsitemsService = newsitemBuilder;
    }
    
       
    public ModelAndView about(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
                    
        populateLocalCommon(mv);             
        mv.addObject("heading", "About");        
        populateSecondaryLatestNewsitems(mv, loggedInUserFilter.getLoggedInUser());
        
        mv.setViewName("about");                     
        return mv;
    }
    
    
    
    
    
      
    public ModelAndView archive(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);

        populateLocalCommon(mv);        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        
        mv.addObject("heading", "Archive");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
            
        List<ArchiveLink> archiveMonths = resourceDAO.getArchiveMonths(loggedInUser != null);
        mv.addObject("archiveLinks", archiveMonths);
                
        mv.setViewName("archiveIndex");
        return mv;
    }
    
    
    public ModelAndView api(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateLocalCommon(mv);
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean showBroken = loggedInUser != null;        
        mv.addObject("heading", "The Wellynews API");

        mv.addObject("feeds", resourceDAO.getAllFeedsByName());
        mv.addObject("publishers", resourceDAO.getAllPublishers(showBroken, true));
        mv.addObject("api_tags", resourceDAO.getTopLevelTags());
        populateSecondaryLatestNewsitems(mv, loggedInUser);        
        mv.setViewName("api");
        return mv;      
    }

    
    
    
    public ModelAndView broken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateLocalCommon(mv);
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
     
        mv.addObject("heading", "Broken sites");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
             
        List<Resource> wrappedCalendars = resourceDAO.getBrokenSites();        
        mv.addObject("main_content", wrappedCalendars);
        mv.setViewName("browse");
        return mv;
    }

    
    
        
    public ModelAndView calendars(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateLocalCommon(mv);
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
     
        mv.addObject("heading", "Calendar Feeds");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
             
        List<Resource> wrappedCalendars = resourceDAO.getAllCalendarFeeds();        
        mv.addObject("main_content", wrappedCalendars);        
        mv.setViewName("calendars");        
        return mv;
    }
    
     
       
    public ModelAndView discovered(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
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

                
    public ModelAndView lastUpdated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
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
        urlStack.setUrlStack(request);
        populateLocalCommon(mv);
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        
        mv.addObject("heading", "All Tags");        
        mv.addObject("tags", resourceDAO.getAllTags());
                
        populateSecondaryLatestNewsitems(mv, loggedInUser);       
        
        mv.setViewName("tags");
        return mv;
    }
    
    
    
    
    
    
    
    
    
    public ModelAndView publishers(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateLocalCommon(mv);
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        boolean showBroken = loggedInUser != null;
        
        mv.addObject("heading", "All Publishers");    
        mv.addObject("publishers", resourceDAO.getAllPublishers(showBroken, true));
        
        populateSecondaryLatestNewsitems(mv, loggedInUser);       
        mv.setViewName("publishers");
        return mv;
    }
    
    
    
    public ModelAndView twitter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        
        User loggedInUser = loggedInUserFilter.getLoggedInUser();
        populateLocalCommon(mv);
        
        final String twitterUsername = siteInformation.getTwitterUsername();
        mv.addObject("twitterUsername", twitterUsername);
        mv.addObject("heading",  "Following the " + siteInformation.getAreaname() + " newslog on Twitter");
        populateLatestTwitters(mv, loggedInUser);
        
        populateSecondaryLatestNewsitems(mv, loggedInUser);

        // TODO permissions
        if(loggedInUser != null) {
        	List<Status> replies = twitterService.getReplies();
			mv.addObject("twitterReplies", replies);
			
			List<TwitteredNewsitem> potentialTwitterSubmissions = twitterNewsitemsService.getPossibleSubmissions();
			mv.addObject("submissions", potentialTwitterSubmissions);			
        }
        
        mv.addObject("main_content", resourceDAO.getTwitterMentionedNewsitems());        
        
        mv.setViewName("twitter");
        return mv;
    }


	
    public ModelAndView watchlist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        urlStack.setUrlStack(request);
        populateLocalCommon(mv);
        
        mv.addObject("heading", "News Watchlist");
        mv.addObject("main_content", resourceDAO.getAllWatchlists(false));

        populateSecondaryLatestNewsitems(mv, loggedInUserFilter.getLoggedInUser());

        setRss(mv, rssUrlBuilder.getRssTitleForWatchlist(), rssUrlBuilder.getRssUrlForWatchlist());
        mv.setViewName("watchlist");
        return mv;
    }
    
    
    @SuppressWarnings("unchecked")
    private void populateLatestTwitters(ModelAndView mv, User loggedInUser) throws IOException {
        boolean showBroken = loggedInUser != null;       
        final List<Newsitem> latestNewsitems = resourceDAO.getLatestTwitteredNewsitems(MAX_TWITTERS_TO_SHOW, showBroken);        
        mv.getModel().put("latest_twitters", latestNewsitems);  
    }
    
    private void populateLocalCommon(ModelAndView mv) {
              mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());
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
    