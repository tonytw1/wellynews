package nz.co.searchwellington.twitter;

import java.util.List;

import net.unto.twitter.TwitterProtos.Status;


public interface TwitterService {

	public List<Status> getReplies();
	
    public String getUsername();

	public boolean isConfigured();

	public Status getTwitById(long twitterId);

}