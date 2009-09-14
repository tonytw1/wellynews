package nz.co.searchwellington.twitter;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.sun.tools.javac.code.Attribute.Array;

import net.unto.twitter.Api;
import net.unto.twitter.Status;
import net.unto.twitter.TwitterException;

public class LiveTwitterService implements TwitterService {
	
    private static final int REPLY_PAGES_TO_FETCH = 5;

	Logger log = Logger.getLogger(LiveTwitterService.class);

    String username;
    String password;

	public LiveTwitterService() {		
	}

		
	public Status[] getReplies() {
		Api api = new Api(username, password);
		log.info("Getting twitter replies from live api for " + username);
		try {			
			Status[] all = new Status[] {};
			for (int i = 1; i < REPLY_PAGES_TO_FETCH; i++) {
				Status[] repliesPage = api.getReplies(i);				
				all = (Status[]) ArrayUtils.addAll(all, repliesPage);
			}
			return all;
			
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
