package nz.co.searchwellington.feeds.rss;

import java.util.List;

import nz.co.searchwellington.feeds.CachingRssfeedNewsitemService;
import nz.co.searchwellington.feeds.FeedReaderRunner;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Component
public class RssNewsitemPrefetcher {
	
	private static Logger log = Logger.getLogger(RssNewsitemPrefetcher.class);

	private HibernateResourceDAO resourceDAO;
	private CachingRssfeedNewsitemService cachingRssfeedNewsitemService;
	private FeedReaderRunner feedReaderRunner;

	private boolean isFeedReadingEnabled = true;
	
	public RssNewsitemPrefetcher() {		
	}
	
	@Autowired
	public RssNewsitemPrefetcher(HibernateResourceDAO resourceDAO,
			CachingRssfeedNewsitemService cachingRssfeedNewsitemService,
			FeedReaderRunner feedReaderRunner) {
		this.resourceDAO = resourceDAO;
		this.cachingRssfeedNewsitemService = cachingRssfeedNewsitemService;
		this.feedReaderRunner = feedReaderRunner;
	}
	
    @Transactional
	public void run() {    	
    	if (!isFeedReadingEnabled) {
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
		final List<Feed> feedsToDecache = Lists.newArrayList();
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
