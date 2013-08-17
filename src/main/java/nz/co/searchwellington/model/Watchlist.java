package nz.co.searchwellington.model;

import java.util.Set;

import uk.co.eelpieconsulting.common.geo.model.Place;

public class Watchlist extends PublishedResourceImpl {
	
	private static final long serialVersionUID = 1L;
	
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

	@Override
	public Place getPlace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPlace(Place place) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}
    
}
