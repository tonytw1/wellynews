package nz.co.searchwellington.model.frontend;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.Twit;
import uk.co.eelpieconsulting.common.views.rss.RssFeedable;

public class FrontendNewsitem extends FrontendResource implements RssFeedable {
	
	private static final long serialVersionUID = 1L;
	
	private String publisherName;
	private List<Twit> retweets;
	private String acceptedFromFeedName;
	private String acceptedByProfilename;
	private List<Comment> comments;
	private Date accepted;
	private FrontendImage image;
	
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

	public Date getAccepted() {
		return accepted;
	}

	public void setAccepted(Date accepted) {
		this.accepted = accepted;
	}
	
	@Override
	public String getAuthor() {
		return publisherName;
	}
	
	public FrontendImage getFrontendImage() {	// TODO rename to getImage
		return image;
	}

	public void setFrontendImage(FrontendImage image) {
		this.image = image;
	}
	
	@Override
	public String getImageUrl() {
		return image != null ? image.getUrl() : null;
	}
	
	@Override
	public String toString() {
		return "FrontendNewsitemImpl [accepted=" + accepted
				+ ", acceptedByProfilename=" + acceptedByProfilename
				+ ", acceptedFromFeedName=" + acceptedFromFeedName
				+ ", comments=" + comments + ", publisherName=" + publisherName
				+ ", retweets=" + retweets + ", getDate()=" + getDate()
				+ ", getDescription()=" + getDescription() + ", getHandTags()="
				+ getHandTags() + ", getHeadline()=" + getHeadline()
				+ ", getHttpStatus()=" + getHttpStatus() + ", getId()="
				+ getId() + ", getImageUrl()=" + getImageUrl()
				+ ", getLatLong()=" + getLatLong() + ", getLiveTime()="
				+ getLiveTime() + ", getName()=" + getName()
				+ ", getOwnerId()=" + getOwnerId() + ", getPlace()="
				+ getPlace() + ", getTags()=" + getTags() + ", getType()="
				+ getType() + ", getUrl()=" + getUrl() + ", getUrlWords()="
				+ getUrlWords() + ", getWebUrl()=" + getWebUrl() + "]";
	}
	
}
