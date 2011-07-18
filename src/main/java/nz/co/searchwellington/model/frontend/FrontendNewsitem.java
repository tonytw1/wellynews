package nz.co.searchwellington.model.frontend;

import java.util.List;

import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.Twit;


public interface FrontendNewsitem extends FrontendResource {
	
	public String getPublisherName();
	public List<Twit> getRetweets();
	public String getAcceptedFromFeedName();
	public List<Comment> getComments();
	
}
