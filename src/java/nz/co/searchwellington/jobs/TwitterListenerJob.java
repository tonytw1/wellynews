package nz.co.searchwellington.jobs;

import java.util.List;

import net.unto.twitter.Status;
import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.TwitteredNewsitem;
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
    private LinkCheckerQueue linkCheckerQueue;
    private ConfigRepository configDAO;
    
  
   
    public TwitterListenerJob() {
    }

    public TwitterListenerJob(TwitterService twitterService, TwitterNewsitemBuilderService newsitemBuilder, ResourceRepository resourceDAO, LinkCheckerQueue linkCheckerQueue, ConfigRepository configDAO) {
        this.twitterService = twitterService;	// This injection no longer needed.
        this.newsitemBuilder = newsitemBuilder;
        this.resourceDAO = resourceDAO;
        this.linkCheckerQueue = linkCheckerQueue;
        this.configDAO= configDAO;
    }

    
    // TODO suppressions? - use feed acceptance deciders?
    @Transactional
    public void run() {

        if (!configDAO.isTwitterListenerEnabled()) {
        	log.info("Twitter listener is not enabled");
        	return;
        }

        log.info("Running Twitter listener");
        if (twitterService.isConfigured()) {

        	Status[] replies = twitterService.getReplies();
			if (replies != null) {
				log.info("Found " + replies.length + " replies.");

				// TODO wire into config switch
				List<TwitteredNewsitem> possibleSubmissions = newsitemBuilder.getPossibleSubmissions();
				//acceptSubmissions(possibleSubmissions);
				
				log.info("Looking or RTs");
				findReTwits(replies);
				
			} else {
				log.warn("Call for Twitter replies returned null.");
			}
			log.info("Twitter listener completed.");
		}
    }

	private void findReTwits(Status[] replies) {		
		newsitemBuilder.getRTs();		
	}

	private void acceptSubmissions(List<TwitteredNewsitem> possibleSubmissions) {
		for (TwitteredNewsitem newsitem : possibleSubmissions) {					
			log.info("Twitted newsitem has title " + newsitem.getName());
			log.info("Twittered newsitem has url: " + newsitem.getUrl());

			if (!resourceDAO.isResourceWithUrl(newsitem.getUrl())) {
				log.info("Saving new newsitem: " + newsitem.getName());
				resourceDAO.saveResource(newsitem);
				linkCheckerQueue.add(newsitem.getId());

				// TODO record datetime that we last recieved a twitter.
				// TODO need to compare / logout the datetime on the twitter vs system time.
			} else {
				log.info("Existing resource on this url; not accepting.");
			}
			
		}
	}

    
    
    
  }




