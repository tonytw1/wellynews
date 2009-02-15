package nz.co.searchwellington.calendars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.util.Date;
import java.util.Iterator;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;


public class IcalParserTest extends TestCase {
    
    
    private static final String TEST_ICAL_FILE = "test/java/nz/co/searchwellington/calendars/downstage.ics";


    private Calendar loadCalendar() throws FileNotFoundException, IOException, ParserException {
        File input = new File(TEST_ICAL_FILE);               
        FileInputStream fin = new FileInputStream(input);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(fin);
        return calendar;
    }
    
    
    private String loadFileToString(File input) throws FileNotFoundException, IOException {
        StringBuffer content = new StringBuffer();
        Reader freader = new FileReader(input);
        BufferedReader in = new BufferedReader(freader);        
        while (in.ready()) {
            content.append(in.readLine());
            content.append("\r\n");
        }
        return content.toString();
    }
    
        
    public void testCanParseCalendarFromString() throws Exception {                       
        File input = new File(TEST_ICAL_FILE);               
        String content = loadFileToString(input);
        
        StringBufferInputStream stream = new StringBufferInputStream(content);        
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(stream);        
        
        assertEquals(358, calendar.getComponents(Component.VEVENT).size());
    }

      
    public void testParseShouldReturnCorrectDateTimeForEvent() throws Exception { 
        Calendar calendar = loadCalendar();
        
        final Iterator eventIterator = calendar.getComponents(Component.VEVENT).iterator();
        VEvent firstEvent = (VEvent) eventIterator.next();
        final Date parsedDate = firstEvent.getStartDate().getDate();
        assertEquals("31 Mar 2008 23:30:00 GMT", parsedDate.toGMTString());
        
        DateTime dt = new DateTime(parsedDate.getTime()); 
        DateTime dtLocal = dt.withZone(DateTimeZone.forID("Pacific/Auckland"));
             
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy MMM dd hh:mm");
        assertEquals("2008 Apr 01 12:30", fmt.print(dtLocal));
    }
    
    
}
