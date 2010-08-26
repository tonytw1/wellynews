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

	static Logger log = Logger.getLogger(LiveTwitterService.class);

	private TwitterApiFactory twitterApiFactory;
	
	
	public LiveTwitterService(TwitterApiFactory twitterApiFactory) {		
		this.twitterApiFactory = twitterApiFactory;
	}

	
	public List<Twit> getReplies() {
		log.info("Getting twitter replies from live api");
		List<Twit> all = new ArrayList<Twit>();
				
        try {        	
        	Twitter receiver = twitterApiFactory.getHttpAuthTwitterApi();
        	
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
    	Twitter receiver = twitterApiFactory.getHttpAuthTwitterApi();
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
	public boolean isConfigured() {
		return twitterApiFactory.isConfigured();
	}

	
	private List<Twit> getRetweets() {
		log.info("Getting twitter retweets from live api");
		List<Twit> all = new ArrayList<Twit>();
        try {        	
        	Twitter receiver = twitterApiFactory.getHttpAuthTwitterApi();
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
