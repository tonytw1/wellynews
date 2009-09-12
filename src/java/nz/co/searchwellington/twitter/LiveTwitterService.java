package nz.co.searchwellington.twitter;

import org.apache.log4j.Logger;

import net.unto.twitter.Api;
import net.unto.twitter.Status;
import net.unto.twitter.TwitterException;

// TODO needs to cache replies.
public class LiveTwitterService implements TwitterService {
	
    Logger log = Logger.getLogger(LiveTwitterService.class);

    String username;
    String password;

	public LiveTwitterService() {		
	}

	
	/* (non-Javadoc)
	 * @see nz.co.searchwellington.twitter.TwitterService#getReplies()
	 */
	public Status[] getReplies() {
		Api api = new Api(username, password);
		log.info("Getting twitter replies from live api for " + username);
		try {
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
