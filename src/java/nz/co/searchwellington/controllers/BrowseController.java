package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.springframework.web.servlet.ModelAndView;


public class BrowseController extends BaseMultiActionController {

    private UrlBuilder urlBuilder;
    private ShowBrokenDecisionService showBrokenDecisionService;
 
	public BrowseController(ResourceRepository resourceDAO, UrlStack urlStack, ConfigRepository configDAO, UrlBuilder urlBuilder, ShowBrokenDecisionService showBrokenDecisionService) {       
		this.resourceDAO = resourceDAO;     
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.urlBuilder = urlBuilder;
        this.showBrokenDecisionService = showBrokenDecisionService;
	}
	
	
	public ModelAndView publisherCalendars(HttpServletRequest request, HttpServletResponse response) throws IOException {
		 ModelAndView mv = new ModelAndView();		 
		 populateLocalCommon(request, mv);		
		 if (request.getAttribute("publisher") != null) {
			 Website publisher = (Website) request.getAttribute("publisher");
			 log.info("Calendar publisher is: " + publisher.getName());
			 populatePublisherCalendars(mv, publisher);             
		 }
		 mv.setViewName("browse");
		 return mv;
	}
	
	
	public ModelAndView publisherFeeds(HttpServletRequest request, HttpServletResponse response) throws IOException {
		 ModelAndView mv = new ModelAndView();		 
		 populateLocalCommon(request, mv);		 
		 if (request.getAttribute("publisher") != null) {
			 Website publisher = (Website) request.getAttribute("publisher");
			 populatePublisherFeeds(mv, publisher);             
		 }
		 mv.setViewName("browse");
		 return mv;
	}
	
	
	
	public ModelAndView publisherWatchlist(HttpServletRequest request, HttpServletResponse response) throws IOException {
		 ModelAndView mv = new ModelAndView();		 
		 populateLocalCommon(request, mv);		 
		 if (request.getAttribute("publisher") != null) {
			 Website publisher = (Website) request.getAttribute("publisher");
			 populatePublisherWatchlist(mv, publisher);             
		 }
		 mv.setViewName("browse");
		 return mv;
	}

 
	
    public ModelAndView archive(HttpServletRequest request, HttpServletResponse response) throws IOException {       
        ModelAndView mv = new ModelAndView();           
        populateLocalCommon(request, mv);
        
        if (request.getAttribute("month") != null) {
            Date month = (Date) request.getAttribute("month");
            mv.addObject("archive_month", new ArchiveLink(month, 0));
            final List<Resource> newsitemsForMonth = resourceDAO.getNewsitemsForMonth(month, showBrokenDecisionService.shouldShowBroken());            
            mv.addObject("main_content", newsitemsForMonth);
			populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());
            mv.addObject("used_tags_description", "Most used tags during this month.");
            
            List<ArchiveLink> archiveLinks = resourceDAO.getArchiveMonths(showBrokenDecisionService.shouldShowBroken());
            populateNextAndPreviousLinks(mv, month, archiveLinks);
            
            populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());
            mv.setViewName("archivePage");
            return mv;
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return null;        
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
                mv.getModel().put("main_content_moreurl", urlBuilder.getArchiveLinkUrl(previous));
            }
            if (indexOf > 0) {
                ArchiveLink next = archiveLinks.get(indexOf-1);
                mv.getModel().put("previous_page", next);
            }                
        }
    }

        
    @SuppressWarnings("unchecked")
    private void populatePublisherCalendars(ModelAndView mv, Website publisher) throws IOException {
        mv.getModel().put("heading", publisher.getName() + " Calendars");       
        mv.getModel().put("main_content", publisher.getCalendars());
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());
    }
    

    @SuppressWarnings("unchecked")  
    private void populatePublisherFeeds(ModelAndView mv, Website publisher) throws IOException {
        mv.getModel().put("heading", publisher.getName() + " Feeds");
        mv.getModel().put("main_content", resourceDAO.getPublisherFeeds(publisher));
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());
    }

    @SuppressWarnings("unchecked")
    private void populatePublisherWatchlist(ModelAndView mv, Website publisher) throws IOException {
        mv.getModel().put("heading", publisher.getName() + " Watchlist items");
        mv.getModel().put("main_content",resourceDAO.getPublisherWatchlist(publisher));
        populateSecondaryLatestNewsitems(mv, showBrokenDecisionService.shouldShowBroken());        
    }
    
    
    @SuppressWarnings("unchecked")
    private void populateLocalCommon(HttpServletRequest request, ModelAndView mv) {
        urlStack.setUrlStack(request);
        mv.getModel().put("top_level_tags", resourceDAO.getTopLevelTags());        
        populateArchiveLinks(mv, showBrokenDecisionService.shouldShowBroken(), resourceDAO.getArchiveMonths(showBrokenDecisionService.shouldShowBroken()));        
    }
	
}
