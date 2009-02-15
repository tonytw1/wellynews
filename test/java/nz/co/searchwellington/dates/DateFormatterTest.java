package nz.co.searchwellington.dates;

import java.util.Calendar;

import org.joda.time.DateTime;

import junit.framework.TestCase;

public class DateFormatterTest extends TestCase {
    
	DateFormatter dateFormatter = new DateFormatter();        
    
    public void testCanMakeW3CFormatForSitemaps() throws Exception {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR, 8);
        now.set(Calendar.MINUTE, 10);
        now.set(Calendar.SECOND, 22);
 
        now.set(Calendar.YEAR, 2008);
        now.set(Calendar.MONTH, 5);
        now.set(Calendar.DAY_OF_MONTH, 9);
        
        final String formatted = dateFormatter.formatW3CDate(now.getTime());
        assertEquals("2008-06-09T08:10:22+01:00", formatted);
    }
    
    
    public void testShouldGiveNiceTimeDeltas() throws Exception {    	
    	DateTime now = new DateTime();
    	
    	DateTime lessThanOneMinuteAgo = now.minusSeconds(30);
    	assertEquals("less than 1 minute ago", dateFormatter.timeSince(lessThanOneMinuteAgo.toDate()));
    	
    	DateTime fiveMinutesBefore = now.minusMinutes(5);
        assertEquals("5 minutes ago", dateFormatter.timeSince(fiveMinutesBefore.toDate()));   
       
        DateTime anHourAgo = now.minusHours(1);
        assertEquals("1 hour ago", dateFormatter.timeSince(anHourAgo.toDate()));
        
        DateTime twoHoursAgo = now.minusHours(2);
        assertEquals("2 hours ago", dateFormatter.timeSince(twoHoursAgo.toDate()));
                
        DateTime oneDayAgo = now.minusDays(1);
        assertEquals("1 day ago", dateFormatter.timeSince(oneDayAgo.toDate()));
        
        DateTime oneWeekAgo = now.minusWeeks(1);
        assertEquals("1 week ago", dateFormatter.timeSince(oneWeekAgo.toDate()));                        
    }
    
}
