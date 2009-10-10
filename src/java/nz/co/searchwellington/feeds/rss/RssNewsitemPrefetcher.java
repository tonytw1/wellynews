package nz.co.searchwellington.feeds.rss;

import java.util.List;

import nz.co.searchwellington.feeds.FeedNewsitemCache;
import nz.co.searchwellington.feeds.FeedReader;
import nz.co.searchwellington.feeds.LiveRssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.springframework.transaction.annotation.Transactional;


public class RssNewsitemPrefetcher {

	private ResourceRepository resourceDAO;
	private LiveRssfeedNewsitemService rssNewsitemService;
	private FeedNewsitemCache feedNewsitemCache;
	private FeedReader feedReader;

	
	public RssNewsitemPrefetcher() {		
	}

	public RssNewsitemPrefetcher(ResourceRepository resourceDAO, LiveRssfeedNewsitemService rssNewsitemService, FeedNewsitemCache feedNewsitemCache, FeedReader feedReader) {
		this.resourceDAO = resourceDAO;
		this.rssNewsitemService = rssNewsitemService;
		this.feedNewsitemCache = feedNewsitemCache;
		this.feedReader = feedReader;
	}

	@Transactional
	public void run() {
		List<Feed> feedsToLoad = resourceDAO.getAllFeeds();
		for (Feed feed : feedsToLoad) {
			if (feed != null) {				
				loadAndCacheFeedNewsitems(feed);
			}
		}
		
		feedReader.acceptFeeditems();
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
