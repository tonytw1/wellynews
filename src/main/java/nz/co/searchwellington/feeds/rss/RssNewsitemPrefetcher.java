package nz.co.searchwellington.feeds.rss;

import java.util.List;

import nz.co.searchwellington.feeds.FeedReaderRunner;
import nz.co.searchwellington.feeds.reading.WhakaoroClientFactory;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

@Component
public class RssNewsitemPrefetcher {
	
	private static Logger log = Logger.getLogger(RssNewsitemPrefetcher.class);

	private HibernateResourceDAO resourceDAO;
	private WhakaoroClientFactory whakaoroClientFactory;
	private FeedReaderRunner feedReaderRunner;
	
	public RssNewsitemPrefetcher() {		
	}
	
	@Autowired
	public RssNewsitemPrefetcher(HibernateResourceDAO resourceDAO, WhakaoroClientFactory whakaoroClientFactory, FeedReaderRunner feedReaderRunner) {
		this.resourceDAO = resourceDAO;	
		this.whakaoroClientFactory = whakaoroClientFactory;
		this.feedReaderRunner = feedReaderRunner;
	}
	
    @Transactional
	public void run() {
		final List<Feed> allFeeds = resourceDAO.getAllFeeds();
    	registerFeedWithWhakaoko(allFeeds);
    	feedReaderRunner.readAllFeeds(allFeeds);
    }

    @Transactional
	private void registerFeedWithWhakaoko(List<Feed> allFeeds) {
		log.info("Registering whakaoro feeds");
		for (Feed feed : (allFeeds)) {
			if (!Strings.isNullOrEmpty(feed.getUrl())) {
				log.info("Registering feed: " + feed.getName());
				final String createdSubscriptionId = whakaoroClientFactory.createFeedSubscription(feed.getUrl());

				log.info("Setting feed whakaoko id to: " + createdSubscriptionId);
				feed.setWhakaokoId(createdSubscriptionId);				
				resourceDAO.saveResource(feed);				
			}
		}
	}

	public void decacheAndLoad(Feed feed) {
		// TODO Auto-generated method stub
	}
	
}
