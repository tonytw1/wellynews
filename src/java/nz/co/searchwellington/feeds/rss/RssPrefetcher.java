package nz.co.searchwellington.feeds.rss;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.sun.syndication.feed.synd.SyndFeed;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.repositories.ResourceRepository;

public class RssPrefetcher {

	private ResourceRepository resourceDAO;
	private RssHttpFetcher rssHttpFetcher;
	private RssCache rssCache;

	
	public RssPrefetcher() {		
	}

	public RssPrefetcher(ResourceRepository resourceDAO, RssHttpFetcher rssHttpFetcher, RssCache rssCache) {
		this.resourceDAO = resourceDAO;
		this.rssHttpFetcher = rssHttpFetcher;
		this.rssCache = rssCache;
	}

	@Transactional
	public void run() {
		List<Feed> feedsToLoad = resourceDAO.getAllFeeds();
		for (Feed feed : feedsToLoad) {
			if (feed != null) {
				final String feedUrl = feed.getUrl();
				if (feedUrl != null) {
					SyndFeed loadedFeed = rssHttpFetcher.httpFetch(feedUrl);
					if (loadedFeed != null) {
						rssCache.putFeedIntoCache(feedUrl, loadedFeed);
					}
				}
			}
		}
	}

	
}
