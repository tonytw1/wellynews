package nz.co.searchwellington.model;

import java.util.Date;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

public class Suggestion {
	
	protected int id;
	protected String url;
	protected Feed feed;
    protected Date firstSeen;
    
	
	public Suggestion() {	
	}

	
	public Suggestion(Feed feed, String url, Date firstSeen) {
		this.feed = feed;
		this.url = url;
		this.firstSeen = firstSeen;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}

	public Feed getFeed() {
		return feed;
	}
	
	public void setFeed(Feed feed) {
		this.feed = feed;
	}
	
	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public Date getFirstSeen() {
		return firstSeen;
	}


	public void setFirstSeen(Date firstSeen) {
		this.firstSeen = firstSeen;
	}
	
}
