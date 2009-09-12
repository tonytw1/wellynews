package nz.co.searchwellington.twitter;

import net.unto.twitter.Status;

public interface TwitterService {

	public Status[] getReplies();

	public boolean isConfigured();

}