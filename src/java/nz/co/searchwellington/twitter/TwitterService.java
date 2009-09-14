package nz.co.searchwellington.twitter;

import net.unto.twitter.Status;

public interface TwitterService {

	public Status[] getReplies();
	
    public String getUsername();

	public boolean isConfigured();

}