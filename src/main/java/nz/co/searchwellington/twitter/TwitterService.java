package nz.co.searchwellington.twitter;

import java.util.List;

import nz.co.searchwellington.model.Twit;

public interface TwitterService {

	public List<Twit> getReplies();
	public Twit getTwitById(long twitterId);
	public String getTwitterProfileImageUrlFor(String twitterUsername);
	
}