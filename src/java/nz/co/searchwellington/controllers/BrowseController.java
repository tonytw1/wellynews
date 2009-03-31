package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.statistics.StatsTracking;

import org.springframework.web.servlet.ModelAndView;


public class BrowseController extends BaseMultiActionController {

    private RequestFilter requestFilter;    
    private RssUrlBuilder rssUrlBuilder;
    
	public BrowseController(ResourceRepository resourceDAO, RequestFilter requestFilter, ItemMaker itemMaker, UrlStack urlStack, ConfigRepository configDAO, RssUrlBuilder rssUrlBuilder) {       
		this.resourceDAO = resourceDAO;     
        this.requestFilter = requestFilter;
        this.itemMaker = itemMaker;
        this.urlStack = urlStack;
        this.configDAO = configDAO;     
        this.rssUrlBuilder = rssUrlBuilder;
	}
	
	
	public ModelAndView publisherCalendars(HttpServletRequest request, HttpServletResponse response) throws IOException {
		 ModelAndView mv = new ModelAndView();		 
		 User loggedInUser = populateLocalCommon(request, mv);           		   
		 requestFilter.loadAttributesOntoRequest(request);
		 if (request.getAttribute("publisher") != null) {
			 Website publisher = (Website) request.getAttribute("publisher");
			 log.info("Calendar publisher is: " + publisher.getName());
			 populatePublisherCalendars(mv, publisher, loggedInUser);             
		 }
		 mv.setViewName("browse");
		 return mv;
	}
	
	
	public ModelAndView publisherFeeds(HttpServletRequest request, HttpServletResponse response) throws IOException {
		 ModelAndView mv = new ModelAndView();		 
		 User loggedInUser = populateLocalCommon(request, mv);           		   
		 requestFilter.loadAttributesOntoRequest(request);
		 if (request.getAttribute("publisher") != null) {
			 Website publisher = (Website) request.getAttribute("publisher");
			 populatePublisherFeeds(mv, publisher, loggedInUser);             
		 }
		 mv.setViewName("browse");
		 return mv;
	}
	
	
	
	public ModelAndView publisherWatchlist(HttpServletRequest request, HttpServletResponse response) throws IOException {
		 ModelAndView mv = new ModelAndView();		 
		 User loggedInUser = populateLocalCommon(request, mv);           		   
		 requestFilter.loadAttributesOntoRequest(request);
		 if (request.getAttribute("publisher") != null) {
			 Website publisher = (Website) request.getAttribute("publisher");
			 populatePublisherWatchlist(mv, publisher, loggedInUser);             
		 }
		 mv.setViewName("browse");
		 return mv;
	}

	
	public ModelAndView publisherNewsitems(HttpServletRequest request, HttpServletResponse response) throws IOException {
		 ModelAndView mv = new ModelAndView();		 
		 User loggedInUser = populateLocalCommon(request, mv);           		   
		 requestFilter.loadAttributesOntoRequest(request);
		 if (request.getAttribute("publisher") != null) {
			 Website publisher = (Website) request.getAttribute("publisher");
			 populatePublisherNewsitems(mv, publisher, loggedInUser);             
		 }
		 mv.setViewName("browse");
		 return mv;
	}
	
	
	public ModelAndView publisherTagCombiner(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ModelAndView mv = new ModelAndView();		 
		 User loggedInUser = populateLocalCommon(request, mv);           		   
		 requestFilter.loadAttributesOntoRequest(request);
		 if (request.getAttribute("publisher") != null && request.getAttribute("tag") != null) {
			 Website publisher = (Website) request.getAttribute("publisher");
			 Tag tag = (Tag) request.getAttribute("tag");
			 populatePublisherTagCombinerNewsitems(mv, publisher, tag, loggedInUser);
		 }
		 mv.setViewName("browse");
		 return mv;		
	}
    
    
 
	@SuppressWarnings("unchecked")
    public ModelAndView archive(HttpServletRequest request, HttpServletResponse response) throws IOException {       
        ModelAndView mv = new ModelAndView();
           
        User loggedInUser = populateLocalCommon(request, mv);
             
        Date month = null;            
        requestFilter.loadAttributesOntoRequest(request);
        if (request.getAttribute("month") != null) {
            month = (Date) request.getAttribute("month");
            mv.getModel().put("archive_month", new ArchiveLink(month, 0));
            final List<Newsitem> newsitemsForMonth = resourceDAO.getNewsitemsForMonth(month);            
            populateMonthArchive(mv, month, loggedInUser, newsitemsForMonth);
            populateUsedTags(mv, loggedInUser, newsitemsForMonth);
            mv.addObject("used_tags_description", "Most used tags during this month.");
            
            
            List<ArchiveLink> archiveLinks = resourceDAO.getArchiveMonths();
            populateNextAndPreviousLinks(mv, month, archiveLinks);
            
            populateSecondaryLatestNewsitems(mv, loggedInUser);
        }
        
        mv.setViewName("archivePage");
        return mv;
    }



    @SuppressWarnings("unchecked")
    private void populateNextAndPreviousLinks(ModelAndView mv, Date month, List<ArchiveLink> archiveLinks) {
        ArchiveLink selected = null;
        for (ArchiveLink link : archiveLinks) {            
            if (link.getMonth().equals(month)) {
                selected = link;
            }                
        }
        
        if (selected != null) {
            // TODO push selected onto the model.            
            final int indexOf = archiveLinks.indexOf(selected);                
            if (indexOf < archiveLinks.size()-1) {
                ArchiveLink previous = archiveLinks.get(indexOf+1);
                mv.getModel().put("next_page", previous);
                mv.getModel().put("main_content_moreurl", previous.getHref());
            }
            if (indexOf > 0) {
                ArchiveLink next = archiveLinks.get(indexOf-1);
                mv.getModel().put("previous_page", next);
            }                
        }
    }

    
    @SuppressWarnings("unchecked")
	public ModelAndView publisherContent(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ModelAndView mv = new ModelAndView();
		       
        User loggedInUser = populateLocalCommon(request, mv);

        requestFilter.loadAttributesOntoRequest(request);
        
        // TODO Could do with nicer urls. 
        // is currently: /browse?publisher=7930&type=W                
        String type = request.getParameter("type");       
        Website publisher = null;
        if (request.getAttribute("publisher") != null) {
            publisher = (Website) request.getAttribute("publisher");
        }
        
        if (publisher != null) {
		    if (type != null && type.equals("F")) {
		        populatePublisherFeeds(mv, publisher, loggedInUser);
            } else if (type != null && type.equals("L")) {
                populatePublisherWatchlist(mv, publisher, loggedInUser);
            } else {
                populatePublisherNewsitems(mv, publisher, loggedInUser);
            }            
        }       
        mv.setViewName("browse");
		return mv;
	}
    
    
    @SuppressWarnings("unchecked")
    private void populateMonthArchive(ModelAndView mv, Date month, User loggedInUser, List<? extends Resource> newsitemsForMonth) throws IOException {             
        mv.getModel().put("main_content", itemMaker.setEditUrls(newsitemsForMonth, loggedInUser));
        populateSecondaryLatestNewsitems(mv, loggedInUser);
    }


    @SuppressWarnings("unchecked")
    private void populatePublisherNewsitems(ModelAndView mv, Website publisher, User loggedInUser) throws IOException {
        boolean showBroken = loggedInUser != null;
        mv.getModel().put("heading", publisher.getName() + " Newsitems");
        final List<Newsitem> publisherNewsitems = resourceDAO.getAllPublisherNewsitems(publisher, showBroken);
        mv.getModel().put("main_content", itemMaker.setEditUrls(publisherNewsitems, loggedInUser));
        if (publisherNewsitems.size() > 0) {            
            setRss(mv, rssUrlBuilder.getRssTitleForPublisher(publisher), rssUrlBuilder.getRssUrlForPublisher(publisher));            
            mv.getModel().put("publisher", publisher);
        }
        populateSecondaryLatestNewsitems(mv, loggedInUser);
    }
    
    
    private void populatePublisherTagCombinerNewsitems(ModelAndView mv, Website publisher, Tag tag, User loggedInUser) throws IOException {
    	  boolean showBroken = loggedInUser != null;
          mv.addObject("heading", publisher.getName() + " + " + tag.getDisplayName() + " newsitems");
          final List<Resource> publisherNewsitems = resourceDAO.getPublisherTagCombinerNewsitems(publisher, tag, showBroken);
          mv.addObject("main_content", itemMaker.setEditUrls(publisherNewsitems, loggedInUser));		
	}
    
    
    @SuppressWarnings("unchecked")
    private void populatePublisherCalendars(ModelAndView mv, Website publisher, User loggedInUser) throws IOException {
        mv.getModel().put("heading", publisher.getName() + " Calendars");
        // TODO can't set edit urls on calendars?
        mv.getModel().put("main_content", publisher.getCalendars());
        populateSecondaryLatestNewsitems(mv, loggedInUser);
    }
    

    @SuppressWarnings("unchecked")  
    private void populatePublisherFeeds(ModelAndView mv, Website publisher, User loggedInUser) throws IOException {
        mv.getModel().put("heading", publisher.getName() + " Feeds");
        mv.getModel().put("main_content", itemMaker.setEditUrls(resourceDAO.getPublisherFeeds(publisher), loggedInUser));
        populateSecondaryLatestNewsitems(mv, loggedInUser);
    }

    @SuppressWarnings("unchecked")
    private void populatePublisherWatchlist(ModelAndView mv, Website publisher, User loggedInUser) throws IOException {
        mv.getModel().put("heading", publisher.getName() + " Watchlist items");
        mv.getModel().put("main_content", itemMaker.setEditUrls(resourceDAO.getPublisherWatchlist(publisher), loggedInUser));
        populateSecondaryLatestNewsitems(mv, loggedInUser);        
    }
    
    
    @SuppressWarnings("unchecked")
    private User populateLocalCommon(HttpServletRequest request, ModelAndView mv) {
        urlStack.setUrlStack(request);
        User loggedInUser = setLoginState(request, mv);
        StatsTracking.setRecordPageImpression(mv, configDAO.getStatsTracking());
        mv.getModel().put("top_level_tags", resourceDAO.getTopLevelTags());
        
        populateArchiveLinks(mv, loggedInUser, resourceDAO.getArchiveMonths());      
        return loggedInUser;
    }
	
}
