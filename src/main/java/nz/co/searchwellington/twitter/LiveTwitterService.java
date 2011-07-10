package nz.co.searchwellington.twitter;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Twit;

import org.apache.log4j.Logger;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class LiveTwitterService implements TwitterService {
	
    //private static final int REPLY_PAGES_TO_FETCH = 1;	// TODO implement

	private static Logger log = Logger.getLogger(LiveTwitterService.class);
	
	private TwitterApiFactory twitterApiFactory;
	
	public LiveTwitterService(TwitterApiFactory twitterApiFactory) {		
		this.twitterApiFactory = twitterApiFactory;
	}

	
	public List<Twit> getReplies() {
		log.info("Getting twitter replies from live api");
		List<Twit> all = new ArrayList<Twit>();
				
        try {
        	Twitter receiver = twitterApiFactory.getOauthedTwitterApi();
        	
        	for (Status status : receiver.getMentions()) {
        		all.add(new Twit(status));        		
        	}        	
        	
        } catch (Exception e) {
        	log.warn("Error during twitter api call: " + e.getMessage());
        }
        
        all.addAll(getRetweets());        
		return all;
	}
	
	
	@Override
	public Twit getTwitById(long statusId) {
		log.info("Getting tweet: " + statusId);
    	Twitter receiver = twitterApiFactory.getOauthedTwitterApi();
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
	
	@Override
	public String getTwitterProfileImageUrlFor(String twitterUsername) {	// TODO Doesn't need to be an authed call.
		log.info("Fetching profile image url for: " + twitterUsername);
    	Twitter receiver = twitterApiFactory.getOauthedTwitterApi();
    	try {
			return receiver.showUser(twitterUsername).getProfileImageURL().toExternalForm();
		} catch (TwitterException e) {
			log.warn("Error during twitter api call: " + e.getMessage());
			return null;
		}
	}
	
	@Override
	public boolean isConfigured() {
		return true; // TODO
	}

	
	private List<Twit> getRetweets() {
		log.info("Getting twitter retweets from live api");
		List<Twit> all = new ArrayList<Twit>();
        try {        	
        	Twitter receiver = twitterApiFactory.getOauthedTwitterApi();
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
	
}
