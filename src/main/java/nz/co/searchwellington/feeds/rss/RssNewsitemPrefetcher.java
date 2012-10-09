package nz.co.searchwellington.feeds.rss;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.feeds.CachingRssfeedNewsitemService;
import nz.co.searchwellington.feeds.FeedReaderRunner;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.repositories.ConfigDAO;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RssNewsitemPrefetcher {
	
	private static Logger log = Logger.getLogger(RssNewsitemPrefetcher.class);

	private HibernateResourceDAO resourceDAO;
	private CachingRssfeedNewsitemService cachingRssfeedNewsitemService;
	private FeedReaderRunner feedReaderRunner;
	private ConfigDAO configDAO;
	
	public RssNewsitemPrefetcher() {		
	}
	
	@Autowired
	public RssNewsitemPrefetcher(HibernateResourceDAO resourceDAO,
			CachingRssfeedNewsitemService cachingRssfeedNewsitemService,
			FeedReaderRunner feedReaderRunner,
			ConfigDAO configDAO) {
		this.resourceDAO = resourceDAO;
		this.cachingRssfeedNewsitemService = cachingRssfeedNewsitemService;
		this.feedReaderRunner = feedReaderRunner;
		this.configDAO = configDAO;
	}
	
    @Transactional
	public void run() {
    	
    	boolean feedsAreEnabled = configDAO.isFeedReadingEnabled();
    	if (!feedsAreEnabled) {
    		log.info("Not prefetching feeds as feeds are disabled by config.");
    		return;
    	}
    	
		final List<Feed> allFeeds = resourceDAO.getAllFeeds();
		for (Feed feed : decideWhichFeedsToDecache(allFeeds)) {
			cachingRssfeedNewsitemService.getFeedNewsitems(feed);
		}
		
		feedReaderRunner.readAllFeeds(allFeeds);
	}
    
    // TODO implement something other than all here
	private List<Feed> decideWhichFeedsToDecache(List<Feed> allFeeds) {
		final List<Feed> feedsToDecache = new ArrayList<Feed>();
		log.info("Deciding which feeds to decache");
		for (Feed feed : allFeeds) {
			log.debug("Feed '" + feed.getName() + "' was last read at: " + feed.getLastRead());
			feedsToDecache.add(feed);
		}
		return feedsToDecache;
	}

	public void decacheAndLoad(Feed feed) {
		cachingRssfeedNewsitemService.decache(feed);
		cachingRssfeedNewsitemService.getFeedNewsitems(feed);
	}
	
}
