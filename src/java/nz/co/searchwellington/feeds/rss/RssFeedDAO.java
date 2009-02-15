package nz.co.searchwellington.feeds.rss;

import com.sun.syndication.feed.synd.SyndFeed;


public class RssFeedDAO {

	private RssCache rssCache;

	public RssFeedDAO(RssCache rssCache) {		
		this.rssCache = rssCache;
	}

	public SyndFeed getFeedByUrl(String url) {
		return rssCache.getFeedByUrl(url);
	}

}
