package nz.co.searchwellington.model;

import java.util.Date;
import java.util.Set;

public class WebsiteImpl extends ResourceImpl implements Website {
	
	private static final long serialVersionUID = 1L;
	
	Set <Feed> feeds;
    Set <Watchlist> watchlist;

    public WebsiteImpl() {        
    }
    
	public WebsiteImpl(int id, String name, String url, Date date, String description, Set<Feed> feed, Set<Watchlist> watchlist, Set<DiscoveredFeed> discoveredFeeds) {
        this.id = id;
		this.name = name;
        this.url = url;
        this.date = date;
        this.description = description;   
        this.feeds = feed;
        this.watchlist = watchlist;
        this.discoveredFeeds = discoveredFeeds;
	}
	
    public Set<Feed> getFeeds() {
        return feeds;
    }
    
    public void setFeeds(Set<Feed> feeds) {
		this.feeds = feeds;
	}

	public Set<Watchlist> getWatchlist() {
        return watchlist;
    }
    
    public String getType() {
        return "W";
     }
  
    public void setWatchlist(Set<Watchlist> watchlist) {
        this.watchlist = watchlist;
    }

}
