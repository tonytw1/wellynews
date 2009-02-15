package nz.co.searchwellington.twitter;

import org.apache.log4j.Logger;

import net.unto.twitter.Api;
import net.unto.twitter.Status;
import net.unto.twitter.TwitterException;

public class TwitterService {
	
    private static final int MAXIMUM_TWITTER_MESSAGE_LENGTH = 140;
    private static final String SEPERATOR = " - ";

    Logger log = Logger.getLogger(TwitterService.class);

    String username;
    String password;

	public TwitterService() {		
	}


	public void twit(String message) {
		Api api = new Api(username, password);		
		log.info("Twittering: " + message);
		try {			
			api.updateStatus(message);
		} catch (TwitterException e) {
			log.error("Twitter exception:", e);
		}
	}


	
    public String buildMessage(String heading, String url) {        
        if (heading.length() + url.length() + SEPERATOR.length() > MAXIMUM_TWITTER_MESSAGE_LENGTH) {
            heading = trimHeadingToLength(heading, MAXIMUM_TWITTER_MESSAGE_LENGTH - (url.length() + SEPERATOR.length()));
        }
        return heading + SEPERATOR + url;      
    }




	public Status[] getReplies() {
		Api api = new Api(username, password);
		log.debug("Getting twitter replies for " + username);
		try {			
			Status[] replies = api.getReplies();			
			return replies;			
		} catch (TwitterException e) {
			log.error("Twitter exception:", e);
		}
		return null;
	}

	// TODO can these be populated with reflection instead?
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
 
    
    
	private String trimHeadingToLength(String heading, int length) {       
		if (heading.length() > length) {
			heading = heading.substring(0, length-1);
		}   
		return heading;
	}
    
    
    
}
