package nz.co.searchwellington.dates;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

public class DateFormatterTest {
    
	DateFormatter dateFormatter = new DateFormatter();        
    
	//@Test TODO enable test
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
    
}
