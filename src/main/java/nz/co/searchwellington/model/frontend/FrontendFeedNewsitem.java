package nz.co.searchwellington.model.frontend;

import nz.co.searchwellington.model.Suggestion;

public class FrontendFeedNewsitem extends FrontendNewsitem {

	private static final long serialVersionUID = 1L;

	private boolean isSuppressed;
	private Suggestion suggestion;
	private Integer localCopy;
	private FrontendFeed feed;
	private FrontendImage image;
	
    public FrontendFeedNewsitem() {
    	setType("FNI");
        this.isSuppressed = false;
    }
    
	public FrontendFeed getFeed() {
		return feed;
	}
	
	public void setFeed(FrontendFeed feed) {
		this.feed = feed;
	}
	
	public FrontendImage getFrontendImage() {
		return image;
	}
	
	public Integer getLocalCopy() {
		return localCopy;
	}
	
	public void setLocalCopy(Integer localCopy) {
		this.localCopy = localCopy;
	}
	
	public boolean isSuppressed() {
		return isSuppressed;
	}

	public void setSuppressed(boolean isSuppressed) {
		this.isSuppressed = isSuppressed;
	}

	public Suggestion getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(Suggestion suggestion) {
		this.suggestion = suggestion;
	}

	public void setImage(FrontendImage image) {
		this.image = image;
	}
	
}