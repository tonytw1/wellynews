package nz.co.searchwellington.model;

import java.util.Set;


public class Watchlist extends PublishedResourceImpl {
    
    public Watchlist() {}
    
        public Watchlist(int id, String name, String url, String description, Website publisher, Set<DiscoveredFeed> discoveredFeeds) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.publisher = publisher;
        this.discoveredFeeds = discoveredFeeds;
    }

    
    public String getType() {
        return "L";
    }
    
}
