package nz.co.searchwellington.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.TwitterMention;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TweetDAO;
import nz.co.searchwellington.utils.UrlCleaner;

import org.apache.log4j.Logger;

public class TwitterNewsitemMentionsFinderService {

    private static Logger log = Logger.getLogger(TwitterNewsitemMentionsFinderService.class);
    
    private UrlCleaner urlCleaner;
	private ResourceRepository resourceDAO;
	private TwitterService twitterService;
	private TweetDAO tweetDAO;
	
	public TwitterNewsitemMentionsFinderService(UrlCleaner urlCleaner,
			ResourceRepository resourceDAO, TwitterService twitterService,
			TweetDAO tweetDAO) {		
		this.urlCleaner = urlCleaner;
		this.resourceDAO = resourceDAO;
		this.twitterService = twitterService;
		this.tweetDAO = tweetDAO;
	}
	
	public List<TwitterMention> getNewsitemMentions() {
		List<TwitterMention> RTs = new ArrayList<TwitterMention>();		

		List<Twit> replies = twitterService.getReplies();
		for (Twit status : replies) {
			final String message = status.getText();			
			log.info("Evaluating tweet: " + message);
			if (status.getInReplyToStatusId() > 0) {							
				final long inReplyTo = status.getInReplyToStatusId();
				log.info("Twit '" + status.getText() + "' is in reply to twit #: " + inReplyTo);
								
				Twit referencedTwit = tweetDAO.loadTweetByTwitterId(inReplyTo);
				if (referencedTwit == null) {
					Twit referencedStatus = twitterService.getTwitById(inReplyTo);
					if (referencedStatus != null) {
						tweetDAO.saveTwit(referencedStatus);						
						referencedTwit = referencedStatus;
					} else {
						log.warn("Failed to load replied to tweet from twitter api: " + inReplyTo);
					}
				}
								
				if (referencedTwit != null) {
					final String referencedTwitMessage = referencedTwit.getText();
					Resource referencedNewsitem = extractReferencedResourceFromMessage(referencedTwitMessage);
					if (referencedNewsitem != null && referencedNewsitem.getType().equals("N")) {
						Twit replyTwit = loadOrCreateTwit(status);
						RTs.add(new TwitterMention((Newsitem) referencedNewsitem, replyTwit));
						log.info("Twit '" + replyTwit + "' is a reply to: " + referencedNewsitem);
					}
					
				} else {
					log.warn("Could not find replied to tweet: " + inReplyTo);
				}
			
			} else if (message != null) {				
				Resource referencedNewsitem = extractReferencedResourceFromMessage(message);					
				if (referencedNewsitem != null && referencedNewsitem.getType().equals("N")) {
					log.info("Found RT: " + referencedNewsitem.getName() + ", " + message);
					Twit tweet = loadOrCreateTwit(status);
					RTs.add(new TwitterMention((Newsitem) referencedNewsitem, tweet));				
				}				
			}	
		}
		return RTs;
	}
	
	private Resource extractReferencedResourceFromMessage(String message) {
		final String url = this.extractUrlFromMessage(message);
		Resource referencedNewsitem = null;
		if (url != null) {
			final String cleanedUrl = urlCleaner.cleanSubmittedItemUrl(url);
			log.debug("Found url '" + cleanedUrl + "' in message: " + message);
			
			referencedNewsitem = resourceDAO.loadResourceByUrl(cleanedUrl); // TOOO load newsitem by url method on DAO instead?
		}
		return referencedNewsitem;
	}
	
	private Twit loadOrCreateTwit(Twit twit) {
		Twit existingTwit = tweetDAO.loadTweetByTwitterId(twit.getTwitterid());
		if (existingTwit == null) {
			log.info("Saving new twit: " + twit.getText());
			tweetDAO.saveTwit(twit);
			return twit;
		}
		// TODO confusing need to have the twitter id as the hibernate id
		return existingTwit;
	}
	
	protected String extractUrlFromMessage(String message) {
		Pattern pattern = Pattern.compile(".*(http://[\\S]+).*");
		Matcher matcher = pattern.matcher(message);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}

}
