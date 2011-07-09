package nz.co.searchwellington.model;
import java.util.Set;

import nz.co.searchwellington.model.frontend.FrontendWebsite;

public interface Website extends Resource, FrontendWebsite {
	
    public Set<Feed> getFeeds();
 
    public Set<Watchlist> getWatchlist();

    public Set<CalendarFeed> getCalendars();

    public void setCalendars(Set<CalendarFeed> calendars);

	public String getUrlWords();
	
}
