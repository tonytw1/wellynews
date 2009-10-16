package nz.co.searchwellington.feeds.rss;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.feeds.FeedNewsitemCache;
import nz.co.searchwellington.feeds.FeedReader;
import nz.co.searchwellington.feeds.LiveRssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.WallClock;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;


public class RssNewsitemPrefetcher {
	
	private static Logger log = Logger.getLogger(RssNewsitemPrefetcher.class);


	private ResourceRepository resourceDAO;
	private LiveRssfeedNewsitemService rssNewsitemService;
	private FeedNewsitemCache feedNewsitemCache;
	private FeedReader feedReader;
	private WallClock wallClock;
	
	public RssNewsitemPrefetcher() {		
	}

	public RssNewsitemPrefetcher(ResourceRepository resourceDAO, LiveRssfeedNewsitemService rssNewsitemService, FeedNewsitemCache feedNewsitemCache, FeedReader feedReader, WallClock wallClock) {
		this.resourceDAO = resourceDAO;
		this.rssNewsitemService = rssNewsitemService;
		this.feedNewsitemCache = feedNewsitemCache;
		this.feedReader = feedReader;
		this.wallClock = wallClock;
	}

	@Transactional
	public void run() {
		List<Feed> allFeeds = resourceDAO.getAllFeeds();
		for (Feed feed : decideWhichFeedsToDecache(allFeeds)) {
			if (feed != null) {
				loadAndCacheFeedNewsitems(feed);
			}
		}
		feedReader.acceptFeeditems();
	}
		
	private List<Feed> decideWhichFeedsToDecache(List<Feed> allFeeds) {
		List<Feed> feedsToDecache = new ArrayList<Feed>();
		log.info("Deciding which feeds to accept");
		log.info("Localtime is: " + wallClock.getLocalWallClockTime() + ". Is daytime: " + wallClock.isCurrentlyDaytime());	
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
