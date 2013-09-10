package nz.co.searchwellington.model.frontend;

public class FrontendFeedNewsitem extends FrontendNewsitem {

	private static final long serialVersionUID = 1L;

	private boolean isSuppressed;
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

	public void setImage(FrontendImage image) {
		this.image = image;
	}
	
}