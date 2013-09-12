package nz.co.searchwellington.feeds.reading;

import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

@Component
public class WhakaokoFeedSyncService {
	
	private static Logger log = Logger.getLogger(WhakaokoFeedSyncService.class);

	private HibernateResourceDAO resourceDAO;
	private WhakaoroClientFactory whakaoroClientFactory;
	
	public WhakaokoFeedSyncService() {		
	}
	
	@Autowired
	public WhakaokoFeedSyncService(HibernateResourceDAO resourceDAO, WhakaoroClientFactory whakaoroClientFactory) {
		this.resourceDAO = resourceDAO;	
		this.whakaoroClientFactory = whakaoroClientFactory;
	}
	
    @Scheduled(fixedRate=3600000)
    @Transactional
	public void run() {
		final List<Feed> allFeeds = resourceDAO.getAllFeeds();
    	registerFeedWithWhakaoko(allFeeds);
    }

    @Transactional
	private void registerFeedWithWhakaoko(List<Feed> allFeeds) {
		log.info("Registering feeds with whakaoro");
		for (Feed feed : (allFeeds)) {
			if (!Strings.isNullOrEmpty(feed.getUrl())) {
				log.info("Registering feed: " + feed.getName());
				final String createdSubscriptionId = whakaoroClientFactory.createFeedSubscription(feed.getUrl());
				log.info("Setting feed whakaoko id to: " + createdSubscriptionId);
				feed.setWhakaokoId(createdSubscriptionId);
				resourceDAO.saveResource(feed);
			}
		}
		log.info("Finished registering feeds with whakaoro");
	}
    
}
