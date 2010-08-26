package nz.co.searchwellington.twitter;

import java.util.List;

import nz.co.searchwellington.model.Twit;


public interface TwitterService {

	public List<Twit> getReplies();
	public Twit getTwitById(long twitterId);
	
	public boolean isConfigured();	// TODO don't like this - should be factories problem.

}