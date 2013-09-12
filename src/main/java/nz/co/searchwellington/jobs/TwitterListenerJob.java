package nz.co.searchwellington.jobs;

import java.util.List;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.TwitterMention;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.twitter.TwitterNewsitemMentionsFinderService;
import nz.co.searchwellington.twitter.TwitterService;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

public class TwitterListenerJob {

    static Logger log = Logger.getLogger(TwitterListenerJob.class);
    
    private TwitterService twitterService;
    private TwitterNewsitemMentionsFinderService twitterMentionFinder;
    private ContentUpdateService contentUpdateService;

	private boolean isTwitterListenerEnabled = true;	// TODO push to properties file
	
    public TwitterListenerJob() {
    }
    
    public TwitterListenerJob(TwitterService twitterService,
			TwitterNewsitemMentionsFinderService twitterMentionFinder,
			ContentUpdateService contentUpdateService) {
		super();
		this.twitterService = twitterService;
		this.twitterMentionFinder = twitterMentionFinder;
		this.contentUpdateService = contentUpdateService;
	}
    
    @Scheduled(fixedRate=3600000)
	@Transactional
    public void run() {
        if (!isTwitterListenerEnabled) {
        	log.info("Twitter listener is not enabled");
        	return;
        }
        
        log.info("Running Twitter listener");
        if (twitterService.isConfigured()) {        	
        	fetchMentions();			
		} else {
			log.warn("Twitter service is not configured; not running");
		}
        log.info("Twitter listener completed.");
	}
	
	private void fetchMentions() {
		List<TwitterMention> newsitemMentions = twitterMentionFinder.getNewsitemMentions();
		for (TwitterMention reTwit : newsitemMentions) {
			Newsitem newsitem = reTwit.getNewsitem();			
			boolean isMentionRT = true;
			if (isMentionRT && !newsitem.getReTwits().contains(reTwit.getTwit())) {				
				log.info("Adding new RT to newsitem: " + reTwit.getTwit().getText());
				newsitem.addReTwit(reTwit.getTwit());
				contentUpdateService.update(newsitem);
			}
		}
	}
  
}
