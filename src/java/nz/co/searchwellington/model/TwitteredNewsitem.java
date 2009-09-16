package nz.co.searchwellington.model;

import java.util.Date;
import java.util.Set;

public class TwitteredNewsitem extends NewsitemImpl {
	
	private Twit twit;
	
    public TwitteredNewsitem(int id, String name, String url, String description, Date date, Website publisher, Set <Tag> tags, Set<DiscoveredFeed> discoveredFeeds, Twit twit) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.date = date;
        this.publisher = publisher;
        this.tags = tags;
        this.discoveredFeeds = discoveredFeeds;
        this.twit = twit;
    }
	
	@Override
	public String getType() {
		return "TNI";
	}

	public Twit getTwit() {
		return twit;
	}

	public void setTwit(Twit twit) {
		this.twit = twit;
	}
	
}
