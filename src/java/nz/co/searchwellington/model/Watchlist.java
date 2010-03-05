package nz.co.searchwellington.model;

import java.util.Set;


public class Watchlist extends PublishedResourceImpl {
    
    public Watchlist() {}
    
    
    // TODO want an interface to say that a class can be parsed for discovered Feeds.
    public Watchlist(int id, String name, String url, String description, Website publisher, Set<Tag> tags, Set<DiscoveredFeed> discoveredFeeds) {
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
    
}
