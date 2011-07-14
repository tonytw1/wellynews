package nz.co.searchwellington.feeds.rss;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sun.syndication.feed.synd.SyndFeed;

public class RssHttpFetcherFunctionalTest {

	@Test
	public void canRetrieveRemoveFeed() throws Exception {
		RssHttpFetcher rssFetcher = new RssHttpFetcher();		
		SyndFeed result = rssFetcher.httpFetch("http://wellington.gen.nz/transport/rss");
		assertEquals(30, result.getEntries().size());
	}
	
}
