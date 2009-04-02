package nz.co.searchwellington.controllers;

import java.io.IOException;
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
import nz.co.searchwellington.statistics.StatsTracking;
import nz.co.searchwellington.twitter.TwitterService;
import nz.co.searchwellington.utils.GoogleMapsDisplayCleaner;

import org.apache.commons.lang.StringEscapeUtils;
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
    
    public SimplePageController(ResourceRepository resourceDAO, ItemMaker itemMaker, UrlStack urlStack, ConfigRepository configDAO, SiteInformation siteInformation, RequestFilter requestFilter, RssUrlBuilder rssUrlBuilder, TwitterService twitterService, DiscoveredFeedRepository discoveredFeedRepository) {
        this.resourceDAO = resourceDAO;
        this.itemMaker = itemMaker;
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.siteInformation = siteInformation;
        this.requestFilter = requestFilter;
        this.rssUrlBuilder = rssUrlBuilder;
        this.twitterService = twitterService;
        this.discoveredFeedRepository = discoveredFeedRepository;
    }
    
       
    public ModelAndView about(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
        
        populateLocalCommon(mv);
                        
        mv.addObject("heading", "About");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
     
        
        mv.setViewName("about");
                        
        return mv;
    }

    
    public ModelAndView archive(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
                                
        mv.addObject("heading", "Archive");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
            
        List<ArchiveLink> archiveMonths = resourceDAO.getArchiveMonths();
        mv.addObject("archiveLinks", archiveMonths);
                
        mv.setViewName("archiveIndex");
        return mv;
    }
    
    
    public ModelAndView calendars(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
     
        mv.addObject("heading", "Calendar Feeds");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
       
      
        List<Resource> wrappedCalendars = itemMaker.wrapCalendars(resourceDAO.getAllCalendarFeeds());        
        mv.addObject("main_content", itemMaker.setEditUrls(wrappedCalendars, loggedInUser));        
        mv.setViewName("calendars");
        
        return mv;
    }
    
    
    
    public ModelAndView commented(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        boolean showBroken = loggedInUser != null;
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
        
        mv.addObject("heading", "Comment");           
        mv.addObject("commented_tags", resourceDAO.getCommentedTags(showBroken));                
        mv.addObject("main_content", itemMaker.setEditUrls(resourceDAO.getAllCommentedNewsitems(500, true), loggedInUser));
        populateSecondaryLatestNewsitems(mv, loggedInUser);
        
        mv.setViewName("commented");
        return mv;      
    }

    
    
    
    public ModelAndView publishers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        boolean showBroken = loggedInUser != null;
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
        
        mv.addObject("heading", "Publishers");
        
        List<Object[]> publisherIds = resourceDAO.getAllPublishers(showBroken, true); 
        for (Object[] objects : publisherIds) {
			int publisherId = (Integer) objects[0];
			Website publisher = (Website) resourceDAO.loadResourceById(publisherId);
			log.info(publisher.getUrlWords());
		
		}
        
        populateSecondaryLatestNewsitems(mv, loggedInUser);        
        mv.setViewName("commented");
        return mv;      
    }


    
       
    public ModelAndView discovered(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
                                
        mv.addObject("heading", "Discovered Feeds");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
        
        List<DiscoveredFeed> nonCommentFeeds = discoveredFeedRepository.getAllNonCommentDiscoveredFeeds();        
		mv.addObject("discovered_feeds", nonCommentFeeds);        
        mv.setViewName("discoveredFeeds");
        return mv;
    }


	
    
    
    

    public ModelAndView feedburner(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
                
        mv.addObject("heading", "FeedBurner");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
        
        log.info("Feedburner widget code is: " + configDAO.getFeedBurnerWidget());
        mv.addObject("feedburner_widget", configDAO.getFeedBurnerWidget());
        mv.addObject("feedburner_widget_escaped", StringEscapeUtils.escapeHtml(configDAO.getFeedBurnerWidget()));
        mv.setViewName("feedburner");
        return mv;
    }

    
    public ModelAndView geotagged(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        boolean showBroken = loggedInUser != null;
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());        
        populateLocalCommon(mv);
                        
        mv.addObject("heading", "Geotagged");
        populateSecondaryLatestNewsitems(mv, loggedInUser);
      
        mv.addObject("geotagged_tags", resourceDAO.getGeotaggedTags(showBroken));
        
        requestFilter.loadAttributesOntoRequest(request);
        
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

    
    protected void populateGeocoded(ModelAndView mv, boolean showBroken, Resource selected) throws IOException {
        List<Resource> geocoded = resourceDAO.getAllValidGeocoded(50, showBroken);
        log.info("Found " + geocoded.size() + " valid geocoded resources.");        
        if (selected != null && !geocoded.contains(selected)) {
        	geocoded.add(selected);
        }
                
        if (geocoded.size() > 0) {
            mv.addObject("main_content", geocoded);
            // TODO inject
            GoogleMapsDisplayCleaner cleaner = new GoogleMapsDisplayCleaner();
            mv.addObject("geocoded", cleaner.dedupe(geocoded, selected));  
            setRss(mv, "Geocoded newsitems RSS Feed", siteInformation.getUrl() + rssUrlBuilder.getRssUrlForGeotagged());
        }
    }

    
    
    
      
    public ModelAndView justin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateLocalCommon(mv);

        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        boolean showBroken = loggedInUser != null;
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
        
        mv.addObject("heading", "Latest Additions");
        mv.addObject("main_content", itemMaker.setEditUrls(resourceDAO.getLatestWebsites(MAX_NEWSITEMS, showBroken), loggedInUser));

        populateSecondaryLatestNewsitems(mv, loggedInUser);

        setRss(mv, rssUrlBuilder.getRssTitleForJustin(), rssUrlBuilder.getRssUrlForJustin());
        mv.setViewName("justin");
        return mv;
    }
    
        
    public ModelAndView lastUpdated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateLocalCommon(mv);

        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());

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
        populateLocalCommon(mv);
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
                        
        mv.addObject("heading", "All Tags");        
        mv.addObject("tags", resourceDAO.getAllTags());
                
        populateSecondaryLatestNewsitems(mv, loggedInUser);
        List<Newsitem> recentNewsitems = resourceDAO.getLatestNewsitems(300, loggedInUser != null);     
        populateUsedTags(mv, loggedInUser, recentNewsitems);
        
        mv.setViewName("tags");
        return mv;
    }
    
    
    
    
    public ModelAndView twitter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
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
        mv.getModel().put("latest_twitters", itemMaker.setEditUrls(latestNewsitems, loggedInUser));  
    }
    
    
    
    
   
    public ModelAndView watchlist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        populateLocalCommon(mv);

        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
        
        mv.addObject("heading", "News Watchlist");
        mv.addObject("main_content", itemMaker.setEditUrls(resourceDAO.getAllWatchlists(), loggedInUser));

        populateSecondaryLatestNewsitems(mv, loggedInUser);

        setRss(mv, rssUrlBuilder.getRssTitleForWatchlist(), rssUrlBuilder.getRssUrlForWatchlist());

        mv.setViewName("watchlist");
        return mv;
    }
    
    
    
    private void populateLocalCommon(ModelAndView mv) {
              mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());
    }
    

}
    