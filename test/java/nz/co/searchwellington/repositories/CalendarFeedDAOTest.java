package nz.co.searchwellington.repositories;



import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;
import net.fortuna.ical4j.model.Calendar;
import nz.co.searchwellington.feeds.calendars.CalendarCache;
import nz.co.searchwellington.model.CalendarFeed;


public class CalendarFeedDAOTest extends TestCase {
    
    CalendarFeedDAO calendarDAO;
    CalendarCache calendarCache;
    CalendarFeed calendarFeed;
    
    @Override
    protected void setUp() throws Exception {    
        calendarCache = mock(CalendarCache.class);
        calendarDAO = new CalendarFeedDAO(calendarCache);    
        calendarFeed = new CalendarFeed();
        calendarFeed.setUrl("http://testdata/ical.ics");
    }
        
    public void testShouldLookInCalendarCacheForPrefetchedCalendarObject() throws Exception {                
        calendarDAO.getCalendarFromFeed(calendarFeed);
        verify(calendarCache).getCalendarFromCache("http://testdata/ical.ics");    
    }
    
    public void testShouldReturnNullIfTheRequestedCalendarIsNotInTheCache() throws Exception {
        stub(calendarCache.getCalendarFromCache("http://testdata/ical.ics")).toReturn(null);
        Calendar calendar = calendarDAO.getCalendarFromFeed(calendarFeed);      
        assertNull(calendar);        
    }
    
}
