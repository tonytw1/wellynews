package nz.co.searchwellington.model.frontend;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.Twit;

public interface FrontendNewsitem extends FrontendResource {
	
	public String getPublisherName();
	public List<Twit> getRetweets();
	public String getAcceptedFromFeedName();
	public String getAcceptedByProfilename();
	public List<Comment> getComments();
	public Date getAccepted();
	
}
