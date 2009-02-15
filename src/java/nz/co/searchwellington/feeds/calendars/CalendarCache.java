package nz.co.searchwellington.feeds.calendars;

import net.fortuna.ical4j.model.Calendar;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

public class CalendarCache {
    
    Logger log = Logger.getLogger(CalendarCache.class);

    public Calendar getCalendarFromCache(String url) {
        try {
            CacheManager manager = CacheManager.create();
            Cache cache = manager.getCache("calendars");            
            if (cache != null) {
                Element cacheElement = cache.get(url);
                if (cacheElement != null) {
                    Calendar calendar = (Calendar) cacheElement.getObjectValue();
                    log.debug("Found calendar for feed in cache: " + url);
                    return calendar;
                }
            }            
        } catch (CacheException e) {
            log.error("CacheException while trying to access calendar from cache.", e);  
        }        
        return null;            
    }
    
    
    public void putCalendarIntoCache(String url, Calendar calendar) {
        try {
            CacheManager manager = CacheManager.create();
            Cache cache = manager.getCache("calendars");
            if (cache != null && calendar != null) {
                Element cachedFeedElement = new Element(url, calendar);
                cache.put(cachedFeedElement);
            }
        } catch (CacheException e) {
            log.error("CacheException while trying to cache calendar.", e);
        }
    }
    
}
