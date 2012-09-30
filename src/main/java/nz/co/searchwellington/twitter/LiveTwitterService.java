package nz.co.searchwellington.twitter;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Twit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@Component
public class LiveTwitterService implements TwitterService {
	
	private static Logger log = Logger.getLogger(LiveTwitterService.class);
	
	private TwitterApiFactory twitterApiFactory;
	
	@Autowired
	public LiveTwitterService(TwitterApiFactory twitterApiFactory) {		
		this.twitterApiFactory = twitterApiFactory;
	}
	
	public List<Twit> getReplies() {
		log.info("Getting twitter replies from live api");
		List<Twit> all = new ArrayList<Twit>();
        try {
        	Twitter api = twitterApiFactory.getOauthedTwitterApi();
        	ResponseList<Status> mentions = api.getMentions();
        	logRateLimitingInformation(api);
        	
        	log.info("Mentions: " + mentions.toString());
			for (Status status : mentions) {
        		all.add(new Twit(status));        		
        	}        	
        	
        } catch (Exception e) {
        	log.warn("Error during twitter api call: " + e.getMessage());
        }
        
        List<Twit> retweets = getRetweets();
    	log.info("Retweets: " + retweets.toString());
		all.addAll(retweets);        
		return all;
	}
	
	@Override
	public Twit getTwitById(long statusId) {
		log.info("Getting tweet: " + statusId);
    	Twitter api = twitterApiFactory.getOauthedTwitterApi();
		try {
			Status status = api.showStatus(statusId);
        	logRateLimitingInformation(api);

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
    	Twitter api = twitterApiFactory.getOauthedTwitterApi();
    	try {
			final String result = api.showUser(twitterUsername).getProfileImageURL().toExternalForm();
			logRateLimitingInformation(api);
			return result;
			
		} catch (TwitterException e) {
			log.warn("Error during twitter api call: " + e.getMessage());
			return null;
		}
	}
	
	@Override
	public boolean isConfigured() {
		return true; // TODO	Move to factory
	}
	
	private List<Twit> getRetweets() {
		log.info("Getting twitter retweets from live api");
		List<Twit> all = new ArrayList<Twit>();
        try {        	
        	Twitter api = twitterApiFactory.getOauthedTwitterApi();
        	List<Status> retweets = api.getRetweetsOfMe();
        	logRateLimitingInformation(api);
			for (Status message : retweets) {
				List<Status> messageRetweets = api.getRetweets(message.getId());
				for (Status retweet : messageRetweets) {
					all.add(new Twit(retweet));
				}
			}
			
        } catch (Exception e) {
        	log.warn("Error during twitter api call: " + e.getMessage());
        }
		return all;
	}
	
	private void logRateLimitingInformation(Twitter api) throws TwitterException {
		final RateLimitStatus rateLimitStatus = api.getRateLimitStatus();
		log.info("Rate limiting information: limit=" + rateLimitStatus.getHourlyLimit() + ", remaining=" + rateLimitStatus.getRemainingHits());
	}
	
}
