package nz.co.searchwellington.model;

public class Suggestion {
	
	protected int id;
	protected String url;
	protected Feed feed;
	
	
	public Suggestion() {	
	}

	
	public Suggestion(Feed feed, String url) {
		this.feed = feed;
		this.url = url;
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
	
}
