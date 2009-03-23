package nz.co.searchwellington.feeds.calendars;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.fortuna.ical4j.model.Calendar;
import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;

public class CalendarPrecachingFetcherTest extends TestCase {
    
    List<Resource> calendarFeeds;
    
    ResourceRepository resourceDAO = mock(ResourceRepository.class);
    CalendarHttpFetcher calendarFetcher = mock(CalendarHttpFetcher.class);
    CalendarCache calendarCache = mock(CalendarCache.class);
    CalendarPrecachingFetcher fetcher;
    
    @Override
    protected void setUp() throws Exception {
        fetcher = new CalendarPrecachingFetcher(resourceDAO, calendarFetcher, calendarCache);
        
        calendarFeeds = new ArrayList<Resource>();
        CalendarFeed firstCalendarFeed = new CalendarFeed();
        firstCalendarFeed.setUrl("http://test/1"); 
        CalendarFeed secondCalendarFeed = new CalendarFeed();
        secondCalendarFeed.setUrl("http://test/2");
        calendarFeeds.add(firstCalendarFeed);
        calendarFeeds.add(secondCalendarFeed);
        
        stub(resourceDAO.getAllCalendarFeeds()).toReturn(calendarFeeds);        
    }

    public void testShouldObtainListOfAllCalenderFeedsToPrefetch() throws Exception {        
        fetcher.run();
        verify(resourceDAO).getAllCalendarFeeds();        
    }
    
    public void testShouldAttemptToFetchEachCalander() throws Exception {                                  
        fetcher.run();        
        verify(calendarFetcher).httpFetch("http://test/1");
        verify(calendarFetcher).httpFetch("http://test/2");            
    }
    
    public void testShouldCacheSuccessfullyLoadedCalendars() throws Exception {
        Calendar firstCalendar = mock(Calendar.class);
        stub(calendarFetcher.httpFetch("http://test/1")).toReturn(firstCalendar);
        stub(calendarFetcher.httpFetch("http://test/2")).toReturn(null);
        fetcher.run();
        verify(calendarCache).putCalendarIntoCache("http://test/1", firstCalendar);
    }
    
    
}
