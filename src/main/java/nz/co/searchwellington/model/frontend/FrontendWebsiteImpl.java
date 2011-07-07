package nz.co.searchwellington.model.frontend;

public class FrontendWebsiteImpl extends FrontendResourceImpl implements FrontendWebsite {

	private int newsitemCount;
	private int feedCount;
	private String urlWords;

	public int getNewsitemCount() {
		return newsitemCount;
	}

	public void setNewsitemCount(int newsitemCount) {
		this.newsitemCount = newsitemCount;
	}

	public void setFeedCount(int feedCount) {
		this.feedCount = feedCount;
	}

	public int getFeedCount() {
		return feedCount;
	}

	public void setUrlWords(String urlWords) {
		this.urlWords = urlWords;
	}
	
	public String getUrlWords() {
		return urlWords;
	}
	
}
