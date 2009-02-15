package nz.co.searchwellington.model;
import java.util.Set;



public interface Website extends Resource {

   
    public Set <Resource> getNewsitems();

    public Set<Feed> getFeeds();
 
    public Set<Watchlist> getWatchlist();

    public Set<CalendarFeed> getCalendars();

    public void setCalendars(Set<CalendarFeed> calendars);

    
}
