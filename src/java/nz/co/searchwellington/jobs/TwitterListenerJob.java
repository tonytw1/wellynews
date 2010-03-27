package nz.co.searchwellington.jobs;

import java.util.List;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.TwitterMention;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.twitter.TwitterNewsitemBuilderService;
import nz.co.searchwellington.twitter.TwitterService;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class TwitterListenerJob {

    static Logger log = Logger.getLogger(TwitterListenerJob.class);
    
    private TwitterService twitterService;
    private TwitterNewsitemBuilderService newsitemBuilder;
    private ConfigRepository configDAO;
    private ContentUpdateService contentUpdateService;
    
    
    public TwitterListenerJob() {
    }

    
    public TwitterListenerJob(TwitterService twitterService,
			TwitterNewsitemBuilderService newsitemBuilder,
			ConfigRepository configDAO,
			ContentUpdateService contentUpdateService) {
		super();
		this.twitterService = twitterService;
		this.newsitemBuilder = newsitemBuilder;
		this.configDAO = configDAO;
		this.contentUpdateService = contentUpdateService;
	}

    
	@Transactional
    public void run() {
        if (!configDAO.isTwitterListenerEnabled()) {
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
		List<TwitterMention> newsitemMentions = newsitemBuilder.getNewsitemMentions();
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
