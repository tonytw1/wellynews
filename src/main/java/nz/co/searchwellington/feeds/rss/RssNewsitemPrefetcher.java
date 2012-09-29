package nz.co.searchwellington.feeds.rss;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.feeds.FeedNewsitemCache;
import nz.co.searchwellington.feeds.FeedReaderRunner;
import nz.co.searchwellington.feeds.LiveRssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RssNewsitemPrefetcher {
	
	private static Logger log = Logger.getLogger(RssNewsitemPrefetcher.class);

	private ResourceRepository resourceDAO;
	private LiveRssfeedNewsitemService rssNewsitemService;
	private FeedNewsitemCache feedNewsitemCache;
	private FeedReaderRunner feedReaderRunner;
	private ConfigRepository configDAO;
	
	public RssNewsitemPrefetcher() {		
	}
	
	@Autowired
	public RssNewsitemPrefetcher(ResourceRepository resourceDAO,
			LiveRssfeedNewsitemService rssNewsitemService,
			FeedNewsitemCache feedNewsitemCache,
			FeedReaderRunner feedReaderRunner,
			ConfigRepository configDAO) {
		this.resourceDAO = resourceDAO;
		this.rssNewsitemService = rssNewsitemService;
		this.feedNewsitemCache = feedNewsitemCache;
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
    	
		List<Feed> allFeeds = resourceDAO.getAllFeeds();
		for (Feed feed : decideWhichFeedsToDecache(allFeeds)) {
			if (feed != null) {
				loadAndCacheFeedNewsitems(feed);
			}
		}		
		feedReaderRunner.readAllFeeds(allFeeds);	
	}
    
    // TODO implement something other than all here
	private List<Feed> decideWhichFeedsToDecache(List<Feed> allFeeds) {
		List<Feed> feedsToDecache = new ArrayList<Feed>();
		log.info("Deciding which feeds to decache");
		for (Feed feed : allFeeds) {
			log.debug("Feed '" + feed.getName() + "' was last read at: " + feed.getLastRead());
			feedsToDecache.add(feed);
		}
		return feedsToDecache;
	}

	public void decacheAndLoad(Feed feed) {
		feedNewsitemCache.decache(feed.getUrl());
		loadAndCacheFeedNewsitems(feed);		
	}
	
	private void loadAndCacheFeedNewsitems(Feed feed) {
		if (feed != null) {
			List<FeedNewsitem> loadedItems = rssNewsitemService.getFeedNewsitems(feed);
			if (loadedItems != null) {
				feedNewsitemCache.putFeedNewsitems(feed.getUrl(), loadedItems);
			}
		}
	}
			
}
