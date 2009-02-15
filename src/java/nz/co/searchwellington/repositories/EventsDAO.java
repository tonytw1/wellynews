package nz.co.searchwellington.repositories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Event;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;

import org.joda.time.DateTime;

public class EventsDAO {
    
    
    private ResourceRepository resourceDAO;
    private CalendarFeedDAO calendarFeedDAO;
    

    public EventsDAO(ResourceRepository resourceDAO, CalendarFeedDAO calendarFeedDAO) {   
        this.resourceDAO = resourceDAO;
        this.calendarFeedDAO = calendarFeedDAO;
    }
    
    
    public List<Event> getAllPendingEvents(int numberToShow) throws IOException {
        final List<Resource> allCalendars = resourceDAO.getAllCalendarFeeds();
        List<Event> allEvents = getPendingEventsFromCalendarFeeds(allCalendars);
        allEvents = trimListToSize(numberToShow, allEvents);
        return allEvents;
        
    }


    public List<Event> getPendingEventsForTag(List<Resource> tagCalendars, int numberToShow) throws IOException {   
        List<Event> tagEvents = getPendingEventsFromCalendarFeeds(tagCalendars);
        tagEvents = trimListToSize(numberToShow, tagEvents);
        return tagEvents;
    }


    private List<Event> trimListToSize(int numberToShow, List<Event> events) {
        if (events.size() > numberToShow) {
            events = events.subList(0, numberToShow);
        }
        return events;
    }


    private List<Event> getPendingEventsFromCalendarFeeds(final List<Resource> tagCalendars) {
        List<Event> tagEvents = new ArrayList<Event>();
          if (tagCalendars.size() > 0) {              
              for (Resource calendarFeed : tagCalendars) {                
                  Calendar calendar =  calendarFeedDAO.getCalendarFromFeed((CalendarFeed) calendarFeed);
                  if (calendar != null) {                     
                      List<Event> events= getPendingEventsFromCalendar(calendar, ((CalendarFeed)calendarFeed).getPublisher());
                      tagEvents.addAll(events);
                  }
              }
              Collections.sort(tagEvents);                            
          }
        return tagEvents;
    }
    
    
    
    
    
    
    public List<Event> getPendingEventsFromCalendar(Calendar calendar, Website publisher) {        
        List<Event> pendingEvents = new ArrayList<Event>();        
        DateTime now = new DateTime();
        for (Event event : getAllEventsFromCalendar(calendar, publisher)) {               
            boolean eventIsInFuture = event.getStartDate() != null && now.isBefore(event.getStartDate().getTime());
            if (eventIsInFuture) {
                pendingEvents.add(event);
            }
        }        
        return pendingEvents;
    }

    
    public List<Event> getAllEventsFromCalendar(Calendar calendar, Website publisher) {
        List<Event> events = new ArrayList<Event>();
        
        final Iterator eventIterator = calendar.getComponents(Component.VEVENT).iterator();
        while (eventIterator.hasNext()) {            
            VEvent vevent = (VEvent) eventIterator.next();            
            Event event = getEventDetails(vevent, publisher);        
            events.add(event);
        }        
        Collections.sort(events);
        return events;
    }


    private Event getEventDetails(VEvent vevent, Website publisher) {        
        String description = null;
        if (vevent.getDescription() != null) {
            description = vevent.getDescription().getValue();
        }
                
        Event event = new Event(vevent.getSummary().getValue(), 
                    vevent.getStartDate().getDate(),
                    vevent.getEndDate().getDate(),
                    description,
                    publisher
                    );
        
        return event;
    }
    
    
    
    

}
