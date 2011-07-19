package nz.co.searchwellington.model.frontend;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.Twit;

public class FrontendNewsitemImpl extends FrontendResourceImpl implements FrontendNewsitem {
	
	private String publisherName;
	private List<Twit> retweets;
	private String acceptedFromFeedName;
	private String acceptedByProfilename;
	private List<Comment> comments;
	private Date accepted;
	
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
	
	@Override
	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public String getAcceptedByProfilename() {
		return acceptedByProfilename;
	}

	public void setAcceptedByProfilename(String acceptedByProfilename) {
		this.acceptedByProfilename = acceptedByProfilename;
	}

	@Override
	public Date getAccepted() {
		return accepted;
	}

	public void setAccepted(Date accepted) {
		this.accepted = accepted;
	}
	
}
