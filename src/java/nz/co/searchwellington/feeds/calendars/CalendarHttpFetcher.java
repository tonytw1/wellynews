package nz.co.searchwellington.feeds.calendars;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.util.CompatibilityHints;
import nz.co.searchwellington.utils.HttpFetcher;

public class CalendarHttpFetcher {
    
    Logger log = Logger.getLogger(CalendarHttpFetcher.class);
    
    private HttpFetcher httpFetcher;
    
    
    public CalendarHttpFetcher(HttpFetcher httpFetcher) {        
        this.httpFetcher = httpFetcher;
    }

    
    public Calendar httpFetch(String url) {
        log.info("Fetching calendar from url: " + url);
        InputStream input = httpFetcher.httpFetch(url);     
        if (input != null)  {
            CalendarBuilder builder = new CalendarBuilder();
            try {
                CompatibilityHints.setHintEnabled( CompatibilityHints.KEY_RELAXED_PARSING, true );              
                Calendar calendar = builder.build(input);             
                input.close();
                return calendar;                          
            } catch (IOException e) {
                log.error("Error loading calendar feed url: " + url, e);              
            } catch (ParserException e) {
                log.error("Error loading calendar feed url: " + url, e);               
            }
        }
        return null;
    }
        
}
