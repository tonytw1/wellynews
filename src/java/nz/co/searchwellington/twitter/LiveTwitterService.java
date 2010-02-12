package nz.co.searchwellington.twitter;

import java.util.ArrayList;
import java.util.List;


import nz.co.searchwellington.model.Twit;

import org.apache.log4j.Logger;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.Status;


public class LiveTwitterService implements TwitterService {
	
    private static final int REPLY_PAGES_TO_FETCH = 1;	// TODO implement

	Logger log = Logger.getLogger(LiveTwitterService.class);

    String username;
    String password;

	public LiveTwitterService() {		
	}

		
	public List<Twit> getReplies() {
		log.info("Getting twitter replies from live api for " + username);
		List<Twit> all = new ArrayList<Twit>();
				
        try {        	
        	Twitter receiver = new TwitterFactory().getInstance(username, password);        
        	for (Status status : receiver.getMentions()) {
        		all.add(new Twit(status));        		
        	}        	
        	
        } catch (Exception e) {
        	log.warn("Error during twitter api call: " + e.getMessage());
        }
        
        all.addAll(getRetweets());        
		return all;
	}
	
	
	
	private List<Twit> getRetweets() {
		log.info("Getting twitter retweets from live api for " + username);
		List<Twit> all = new ArrayList<Twit>();				
        try {        	
        	Twitter receiver = new TwitterFactory().getInstance(username, password);        
        	List<Status> retweets = receiver.getRetweetsOfMe();
             for (Status message : retweets) {          
                 List<Status> messageRetweets = receiver.getRetweets(message.getId());
                 for (Status retweet : messageRetweets) {
                	 all.add(new Twit(retweet));
                 }                 
             }
        	
        } catch (Exception e) {
        	log.warn("Error during twitter api call: " + e.getMessage());
        }
		return all;
	}
	
	
	

	
	@Override
	public Twit getTwitById(long statusId) {
		log.info("Getting tweet: " + statusId);
		Twitter receiver = new TwitterFactory().getInstance(username, password);
		try {
			Status status = receiver.showStatus(statusId);
			if (status != null) {
				return new Twit(status);
			}
		} catch (TwitterException e) {
			log.warn("Error during twitter api call: " + e.getMessage());
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
