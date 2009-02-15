package nz.co.searchwellington.repositories;

import net.fortuna.ical4j.model.Calendar;
import nz.co.searchwellington.feeds.calendars.CalendarCache;
import nz.co.searchwellington.model.CalendarFeed;

public class CalendarFeedDAO {

    CalendarCache calendarCache;
    
    public CalendarFeedDAO(CalendarCache calendarCache) {
        this.calendarCache = calendarCache;
    }
    
    public Calendar getCalendarFromFeed(CalendarFeed calendarFeed) {
        if (calendarFeed != null && calendarFeed.getUrl() != null) {
            return calendarCache.getCalendarFromCache(calendarFeed.getUrl());
        }
        return null;
    }

}
