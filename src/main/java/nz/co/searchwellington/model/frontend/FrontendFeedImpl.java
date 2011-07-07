package nz.co.searchwellington.model.frontend;

public class FrontendFeedImpl extends FrontendResourceImpl implements FrontendFeed {

	private String publisherName;
	private String urlWords;
	
	public String getPublisherName() {
		return publisherName;
	}

	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	public String getUrlWords() {
		return urlWords;
	}

	public void setUrlWords(String urlWords) {
		this.urlWords = urlWords;
	}
	
}
