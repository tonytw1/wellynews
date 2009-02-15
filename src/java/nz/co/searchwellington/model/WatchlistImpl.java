package nz.co.searchwellington.model;

import java.util.Set;

import nz.co.searchwellington.dates.DateFormatter;

import com.sun.syndication.feed.synd.SyndEntry;


public class WatchlistImpl extends PublishedResourceImpl implements Watchlist {
    
    public WatchlistImpl() {}
    
    
    // TODO want an interface to say that a class can be parsed for discovered Feeds.
    public WatchlistImpl(int id, String name, String url, String description, Website publisher, Set<Tag> tags, Set<DiscoveredFeed> discoveredFeeds) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.publisher = publisher;
        this.tags = tags;
        this.discoveredFeeds = discoveredFeeds;
    }

    
    public String getType() {
        return "L";
    }

    
    public SyndEntry getRssItem() {
        SyndEntry entry = super.getRssItem();        
        if (getLastChanged() != null) {
            // TODO this is abit odd; suggests RSS should not be on the model.
            DateFormatter dateFormatter = new DateFormatter();
            entry.setTitle(name + " - " + dateFormatter.formatDate(getLastChanged(), "d MMM yyyy"));
        }
        return entry;
    }

    
        
}
