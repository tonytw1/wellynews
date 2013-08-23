package nz.co.searchwellington.model.frontend;

import java.util.Date;

public class FrontendFeed extends FrontendResource {

	private static final long serialVersionUID = 1L;
	
	private String publisherName;
	private String urlWords;
	private Date latestItemDate;
	
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
	
	final public Date getLatestItemDate() {
		return latestItemDate;
	}
	
	final public void setLatestItemDate(Date latestItemDate) {
		this.latestItemDate = latestItemDate;	
	}
	
}
