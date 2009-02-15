package nz.co.searchwellington.feeds.calendars;

import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class CalendarPrecachingFetcher {

    Logger log = Logger.getLogger(CalendarPrecachingFetcher.class);
    
    private ResourceRepository resourceDAO;
    private CalendarHttpFetcher calendarFetcher;
    private CalendarCache calendarCache;
    
    
    public CalendarPrecachingFetcher() {       
    }


    public CalendarPrecachingFetcher(ResourceRepository resourceDAO, CalendarHttpFetcher calendarFetcher, CalendarCache calendarCache) {
        this.resourceDAO = resourceDAO;
        this.calendarFetcher = calendarFetcher;
        this.calendarCache = calendarCache;
    }

    
    @Transactional()
    public void run() {
        log.info("Prefetching calendar feeds");
        List<Resource> calendarFeeds = resourceDAO.getAllCalendarFeeds();
        for (Resource calendarFeed : calendarFeeds) {
            log.info("Loading calendar: " + calendarFeed.getName());
            if (calendarFeed.getUrl() != null) {
                Calendar loadedCalendar = calendarFetcher.httpFetch(calendarFeed.getUrl());
                if (loadedCalendar != null) {
                    calendarCache.putCalendarIntoCache(calendarFeed.getUrl(), loadedCalendar);
                } else {
                    log.warn("Failed to load calendar: " + calendarFeed.getName());
                }
            }
        }
        log.info("Finished loading calendars");
    }

}
