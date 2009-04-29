package nz.co.searchwellington.jobs;

import net.unto.twitter.Status;
import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Newsitem;
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
        this.twitterService = twitterService;
        this.newsitemBuilder = newsitemBuilder;
        this.resourceDAO = resourceDAO;
        this.linkCheckerQueue = linkCheckerQueue;
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
			Status[] replies = twitterService.getReplies();
			if (replies != null) {
				log.info("Found " + replies.length + " replies.");

				for (Status status : replies) {
					log.info("reply: " + status.getText());

					// TODO only want to process replies newer than the last processed twitter.

					Newsitem newsitem = resourceDAO.createNewNewsitem();
					newsitemBuilder.createNewsitemFromTwitterReply(status
							.getText(), newsitem, status.getUser()
							.getScreenName());
					
				
					if (newsitem != null && newsitem.getUrl() != null && !newsitem.getUrl().equals("")) {
						log.info("Twitted newsitem has title " + newsitem.getName());
						log.info("Twittered newsitem has url: " + newsitem.getUrl());

						boolean isRT = newsitem.getName() != null & newsitem.getName().startsWith("RT");
						if (!isRT) {
							if (!resourceDAO.isResourceWithUrl(newsitem.getUrl())) {
								log.info("Saving new newsitem: " + newsitem.getName());

								resourceDAO.saveResource(newsitem);
								linkCheckerQueue.add(newsitem.getId());

								// TODO record datetime that we last recieved a twitter.
								// TODO need to compare / logout the datetime on the twitter vs system time.
							} else {
								log.info("Existing resource on this url; not accepting.");
							}
						} else {
							log.info("Looks like a RT; not accepting");
						}
					}
				}
			} else {
				log.warn("Call for Twitter replies returned null.");
			}
			log.info("Twitter listener completed.");
		}
    }

  }
