package nz.co.searchwellington.feeds;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import nz.co.searchwellington.caching.MemcachedCache;
import nz.co.searchwellington.model.FeedNewsitem;

public class FeedNewsitemCacheTest {

	private static final String FEED_URL = "http://feed/url";
	
	@Mock MemcachedCache cache;
	@Mock List<FeedNewsitem> feedNewsItems;
	
	private FeedNewsitemCache feedNewsitemCache;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		feedNewsitemCache = new FeedNewsitemCache(cache);
	}
	
	@Test
	public void feedNewsitemsShouldBeCachedForOneDay() throws Exception {
		feedNewsitemCache.putFeedNewsitems(FEED_URL, feedNewsItems);		
		Mockito.verify(cache).put("rssfeednewsitems:" + FEED_URL, 24 * 3600, feedNewsItems);		
	}
	
	@Test
	public void feedDecacheShouldClearFeedFromMemcache() throws Exception {
		feedNewsitemCache.decache(FEED_URL);
		Mockito.verify(cache).decache(FEED_URL);
	}
	
}
