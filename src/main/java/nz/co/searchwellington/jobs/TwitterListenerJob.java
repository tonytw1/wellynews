package nz.co.searchwellington.jobs;

import java.util.List;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.TwitterMention;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.twitter.TwitterNewsitemMentionsFinderService;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

public class TwitterListenerJob {

    static Logger log = Logger.getLogger(TwitterListenerJob.class);
    
    private TwitterNewsitemMentionsFinderService twitterMentionFinder;
    private ContentUpdateService contentUpdateService;

	private boolean isTwitterListenerEnabled = true;	// TODO push to properties file
	
    public TwitterListenerJob() {
    }
    
    public TwitterListenerJob(TwitterNewsitemMentionsFinderService twitterMentionFinder,
			ContentUpdateService contentUpdateService) {
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
        fetchMentions();
        
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
