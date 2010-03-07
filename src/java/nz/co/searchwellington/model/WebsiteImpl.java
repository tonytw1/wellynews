package nz.co.searchwellington.model;

import java.util.Date;
import java.util.Set;



public class WebsiteImpl extends ResourceImpl implements Website {
   
    Set <Feed> feeds;
    Set <Watchlist> watchlist;
    Set <CalendarFeed> calendars;
 
	
    
    public WebsiteImpl() {        
    }
    
	public WebsiteImpl(int id, String name, String url, Date date, String description, Set<Feed> feed, Set<Watchlist> watchlist, Set<Tag> tags, Set<DiscoveredFeed> discoveredFeeds, Set<CalendarFeed> calendars) {
        this.id = id;
		this.name = name;
        this.url = url;
        this.date = date;
        this.description = description;
        
        this.feeds = feed;
        this.watchlist = watchlist;
        this.calendars = calendars;
        
        // TODO can these shared ones go up into the super constuctor?
        this.tags = tags;
        this.discoveredFeeds = discoveredFeeds;
	}
	
    public Set<Feed> getFeeds() {
        return feeds;
    }


    public Set<Watchlist> getWatchlist() {
        return watchlist;
    }


 
    
    public String getType() {
        return "W";
     }



    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void setFeeds(Set<Feed> feeds) {
        this.feeds = feeds;
    }

    public void setWatchlist(Set<Watchlist> watchlist) {
        this.watchlist = watchlist;
    }

    public Set<CalendarFeed> getCalendars() {
        return calendars;
    }

    public void setCalendars(Set<CalendarFeed> calendars) {
        this.calendars = calendars;
    }
        
}
