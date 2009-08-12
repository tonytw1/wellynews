package nz.co.searchwellington.model;

import java.util.Date;
import java.util.Set;

public class TwitteredNewsitem extends NewsitemImpl {
	
	private long twitterId;

    public TwitteredNewsitem(int id, String name, String url, String description, Date date, Website publisher, Set <Tag> tags, Set<DiscoveredFeed> discoveredFeeds, long twitterId) {       
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.date = date;
        this.publisher = publisher;
        this.tags = tags;
        this.discoveredFeeds = discoveredFeeds;
        this.twitterId  = twitterId;
    }
	
	@Override
	public String getType() {
		return "TNI";
	}

	public long getTwitterId() {
		return twitterId;
	}

	public void setTwitterId(long twitterId) {
		this.twitterId = twitterId;
	}
	
}
