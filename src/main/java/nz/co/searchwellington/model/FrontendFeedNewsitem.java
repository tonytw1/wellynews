package nz.co.searchwellington.model;	// TODO move to frontend package

import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendImage;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;

public class FrontendFeedNewsitem extends FrontendNewsitemImpl implements FrontendNewsitem {

	private static final long serialVersionUID = 1L;

	private boolean isSuppressed;
	private Suggestion suggestion;
	private Integer localCopy;
	private FrontendFeed feed;
	private FrontendImage image;

	private String publisherName;
	
    public FrontendFeedNewsitem() {
        this.isSuppressed = false;
    }
    
	public FrontendFeed getFeed() {
		return feed;
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

	public String getPublisherName() {
		return publisherName;
	}

	public void setImage(FrontendImage image) {
		this.image = image;
	}

	public void setFeed(FrontendFeed feed) {
		this.feed = feed;
	}
	
}