package nz.co.searchwellington.model.frontend;

public class FrontendFeedImpl extends FrontendResourceImpl implements FrontendFeed {

	private static final long serialVersionUID = 1L;
	
	private String publisherName;
	private String urlWords;
	
	final public String getPublisherName() {
		return publisherName;
	}

	final public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	final public String getUrlWords() {
		return urlWords;
	}

	final public void setUrlWords(String urlWords) {
		this.urlWords = urlWords;
	}
	
}
