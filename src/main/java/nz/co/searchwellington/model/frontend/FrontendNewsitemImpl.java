package nz.co.searchwellington.model.frontend;

import java.util.List;

import nz.co.searchwellington.model.Twit;

public class FrontendNewsitemImpl extends FrontendResourceImpl implements FrontendNewsitem {
	
	private String publisherName;
	private List<Twit> retweets;
	private String acceptedFromFeedName;
	
	public String getPublisherName() {
		return publisherName;
	}
	
	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	public List<Twit> getRetweets() {
		return retweets;
	}

	public void setRetweets(List<Twit> retweets) {
		this.retweets = retweets;
	}

	public String getAcceptedFromFeedName() {
		return acceptedFromFeedName;
	}

	public void setAcceptedFromFeedName(String acceptedFromFeedName) {
		this.acceptedFromFeedName = acceptedFromFeedName;
	}
	
}
