package nz.co.searchwellington.feeds.rss;

import java.net.URL;

import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;

public class RssHttpFetcher {

	private static final int CONNECT_TIMEOUT = 10000;
	private static final int READ_TIMEOUT = 30000;
	
	Logger log = Logger.getLogger(RssHttpFetcher.class);
	private String userAgent;

	public SyndFeed httpFetch(String feedUrl) {
		log.info("Fetching rss from live url: " + feedUrl);
		try {
			URL url = new URL(feedUrl);
			HttpClientFeedFetcher fetcher = new HttpClientFeedFetcher();
			if (userAgent != null) {
				fetcher.setUserAgent(userAgent);
			}
			fetcher.setConnectTimeout(CONNECT_TIMEOUT);
			fetcher.setReadTimeout(READ_TIMEOUT);
			SyndFeed feed = fetcher.retrieveFeed(url);
			if (feed != null) {
				return feed;
			}
			log.warn("Rss feed was null after loading from: " + feedUrl);
			
		} catch (Exception e) {
			log.warn("Error while fetching feed" ,e);
		}
		return null;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
}
