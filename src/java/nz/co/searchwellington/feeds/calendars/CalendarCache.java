package nz.co.searchwellington.feeds.calendars;

import net.fortuna.ical4j.model.Calendar;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;

public class CalendarCache {
    
    Logger log = Logger.getLogger(CalendarCache.class);

    private CacheManager manager;

   
    public CalendarCache(CacheManager manager) {		
		this.manager = manager;
	}


	public Calendar getCalendarFromCache(String url) {
		Cache cache = manager.getCache("calendars");
		if (cache != null) {
			Element cacheElement = cache.get(url);
			if (cacheElement != null) {
				Calendar calendar = (Calendar) cacheElement.getObjectValue();
				log.debug("Found calendar for feed in cache: " + url);
				return calendar;
			}
		}

		return null;
	}

	public void putCalendarIntoCache(String url, Calendar calendar) {
		Cache cache = manager.getCache("calendars");
		if (cache != null && calendar != null) {
			Element cachedFeedElement = new Element(url, calendar);
			cache.put(cachedFeedElement);
		}
	}
    
}
