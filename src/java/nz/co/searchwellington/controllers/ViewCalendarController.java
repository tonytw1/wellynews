package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.model.Calendar;
import nz.co.searchwellington.filters.RequestFilter;
import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Event;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.CalendarFeedDAO;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.EventsDAO;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;



public class ViewCalendarController extends BaseMultiActionController {
    
    Logger log = Logger.getLogger(ViewCalendarController.class);
    
    private RequestFilter requestFilter;

    private CalendarFeedDAO calendarFeedDAO;
    private EventsDAO eventsDAO;
   
    
    public ViewCalendarController(ResourceRepository resourceDAO, RequestFilter requestFilter, UrlStack urlStack, ConfigRepository configDAO, CalendarFeedDAO calendarFeedDAO, EventsDAO eventsDAO, LoggedInUserFilter loggedInUserFilter) {
        this.resourceDAO = resourceDAO;
        this.requestFilter = requestFilter;        
        this.urlStack = urlStack;
        this.configDAO = configDAO;
        this.calendarFeedDAO = calendarFeedDAO;
        this.eventsDAO = eventsDAO;
        this.loggedInUserFilter = loggedInUserFilter;
    }

    
    
    @Transactional
    public ModelAndView viewcalendar(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        ModelAndView mv = new ModelAndView();

        urlStack.setUrlStack(request);
        User loggedInUser = loggedInUserFilter.getLoggedInUser();                     
        mv.addObject("top_level_tags", resourceDAO.getTopLevelTags());
        
       

        CalendarFeed calendarFeed = null;
        if (request.getAttribute("calendarfeed") != null) {
            calendarFeed = (CalendarFeed) request.getAttribute("calendarfeed");            
            mv.addObject("heading", calendarFeed.getName());
            
            
            Calendar calendar = calendarFeedDAO.getCalendarFromFeed(calendarFeed);
            if (calendar != null) {               
                List<Event> events = eventsDAO.getPendingEventsFromCalendar(calendar, calendarFeed.getPublisher());
                mv.addObject("events", events);
            }
            
        }

        if (calendarFeed != null) {                       
            mv.addObject("calendarfeed", calendarFeed);                                    
        } else {
            throw new IllegalArgumentException("Invalid Feed identifier.");
        }
        
        populateSecondaryCalendars(mv, loggedInUser); 
          
        mv.setViewName("viewcalendar");
        return mv;
    }

    
    
    final protected void populateSecondaryCalendars(ModelAndView mv, User loggedInUser) {      
        mv.addObject("righthand_heading", "Calendars");                
        mv.addObject("righthand_description", "Calendars published by local organisations.");               
        final List<Resource> allCalendars = resourceDAO.getAllCalendarFeeds();
        if (allCalendars.size() > 0) {
            mv.addObject("righthand_content", allCalendars);               
        }
    }
    

    
}
