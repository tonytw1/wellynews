package nz.co.searchwellington.model;

import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendImage;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;

public class FeedNewsitem extends FrontendNewsitemImpl implements FrontendNewsitem {
	
	private static final long serialVersionUID = 1L;
	
	private final FrontendFeed feed;
	private FrontendImage image;
	
	public FeedNewsitem(FrontendFeed feed) {
		this.feed = feed;
	}

	@Override
	public String getType() {
		return "FNI";
	}
	
	public FrontendImage getImage() {
		return image;
	}

	public void setImage(FrontendImage image) {
		this.image = image;
	}
	
	public FrontendFeed getFeed() {
		return feed;
	}

	@Override
	public String toString() {
		return "FeedNewsitem [feed=" + feed + ", image=" + image + "]";
	}
	
}
