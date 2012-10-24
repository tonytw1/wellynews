package nz.co.searchwellington.feeds.rss;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.feeds.CachingRssfeedNewsitemService;
import nz.co.searchwellington.feeds.FeedReaderRunner;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedImpl;
import nz.co.searchwellington.repositories.ConfigDAO;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class RssPrefetcherTest {
	
	@Mock private HibernateResourceDAO resourceDAO;
	@Mock private CachingRssfeedNewsitemService cachingRssfeedNewsiemService;
	@Mock private ConfigDAO configDAO;
	@Mock private FeedReaderRunner feedReaderRunner;
	
	Feed firstFeed;
	Feed secondFeed;
	List<Feed> feeds;
	
	RssNewsitemPrefetcher prefetcher;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		firstFeed = new FeedImpl();
		firstFeed.setUrl("http://testdata/rss/1");

		secondFeed = new FeedImpl();
		secondFeed.setUrl("http://testdata/rss/2");

		feeds = Lists.newArrayList();
		feeds.add(firstFeed);
		feeds.add(secondFeed);

		when(configDAO.isFeedReadingEnabled()).thenReturn(true);
		when(resourceDAO.getAllFeeds()).thenReturn(feeds);
		
		prefetcher = new RssNewsitemPrefetcher(resourceDAO, cachingRssfeedNewsiemService, feedReaderRunner, configDAO);
	}
	
	@Test
	public void testShouldLoadListOfAllFeedsToPrefetch() throws Exception {		
		prefetcher.run();
		
		verify(resourceDAO).getAllFeeds();
	}
	
	@Test
	public void testShouldFetchAndCacheAllFeeds() throws Exception {		
		prefetcher.run();
		
		verify(cachingRssfeedNewsiemService).getFeedNewsitems(firstFeed);
		verify(cachingRssfeedNewsiemService).getFeedNewsitems(secondFeed);
		// TODO doesn't verify cache put
	}
	
	@Test
	public void testShouldPerformFeedReadingRightAfterPrefetching() throws Exception {
		prefetcher.run();
		
		verify(feedReaderRunner).readAllFeeds(feeds);
	}

}
