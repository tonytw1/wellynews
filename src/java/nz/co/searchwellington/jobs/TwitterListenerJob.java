package nz.co.searchwellington.jobs;

import java.util.List;

import net.unto.twitter.TwitterProtos.Status;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.TwitterMention;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.twitter.TwitterNewsitemBuilderService;
import nz.co.searchwellington.twitter.TwitterService;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class TwitterListenerJob {

    Logger log = Logger.getLogger(TwitterListenerJob.class);
    
    private TwitterService twitterService;
    private TwitterNewsitemBuilderService newsitemBuilder;
    private ResourceRepository resourceDAO;
    private ConfigRepository configDAO;
    
    
    public TwitterListenerJob() {
    }

    public TwitterListenerJob(TwitterService twitterService, TwitterNewsitemBuilderService newsitemBuilder, 
    		ResourceRepository resourceDAO, ConfigRepository configDAO) {
        this.twitterService = twitterService;
        this.newsitemBuilder = newsitemBuilder;
        this.resourceDAO = resourceDAO;
        this.configDAO= configDAO;
    }

    
    @Transactional
    public void run() {
        if (!configDAO.isTwitterListenerEnabled()) {
        	log.info("Twitter listener is not enabled");
        	return;
        }
        
        log.info("Running Twitter listener");
        if (twitterService.isConfigured()) {
        	refreshTweets();
        	fetchMentions();			
		} else {
			log.warn("Twitter service is not configured; not running");
		}
        log.info("Twitter listener completed.");
    }

    
    // TODO this should be on an admin controller.
	private void refreshTweets() {
		List<Twit> allLocalTwits = resourceDAO.getAllTweets();
		for (Twit twit : allLocalTwits) {
			if (twit.getDate() == null) {
				Status status = twitterService.getTwitById(twit.getTwitterid());
				if (status != null) {
					twit.setDate(new Twit(status).getDate());
					resourceDAO.saveTweet(twit);
					log.info("Tweet date for '" + twit.getText() + "' updated to: " + twit.getDate());
				
				} else {
					log.warn("Could not load tweet #" + twit.getTwitterid().toString() + " from api");
				}
			}
		}
	}

	
	private void fetchMentions() {
		List<TwitterMention> newsitemMentions = newsitemBuilder.getNewsitemMentions();
		for (TwitterMention reTwit : newsitemMentions) {
			Newsitem newsitem = reTwit.getNewsitem();			
			boolean isMentionRT = true;
			if (isMentionRT && !newsitem.getReTwits().contains(reTwit.getTwit())) {				
				log.info("Adding new RT to newsitem: " + reTwit.getTwit().getText());
				newsitem.addReTwit(reTwit.getTwit());
				resourceDAO.saveResource(newsitem);				
			}
		}
	}
  
}
