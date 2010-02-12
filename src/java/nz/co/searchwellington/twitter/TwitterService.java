package nz.co.searchwellington.twitter;

import java.util.List;

import nz.co.searchwellington.model.Twit;


public interface TwitterService {

	public List<Twit> getReplies();
	
    public String getUsername();

	public boolean isConfigured();

	public Twit getTwitById(long twitterId);

}