package nz.co.searchwellington.twitter;

import org.apache.log4j.Logger;

import net.unto.twitter.Api;
import net.unto.twitter.Status;
import net.unto.twitter.TwitterException;

// TODO needs to cache replies.
public class TwitterService {
	
    Logger log = Logger.getLogger(TwitterService.class);

    String username;
    String password;

	public TwitterService() {		
	}

	
	public Status[] getReplies() {
		Api api = new Api(username, password);
		log.debug("Getting twitter replies for " + username);
		try {
			// TODO This needs to be cached.
			Status[] replies = api.getReplies();			
			return replies;			
		} catch (TwitterException e) {
			log.error("Twitter exception:", e);
		}
		return null;
	}


	public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


	public boolean isConfigured() {
		return this.username != null && !this.username.equals("") && this.password != null && !this.password.equals("");
	}
		
}
