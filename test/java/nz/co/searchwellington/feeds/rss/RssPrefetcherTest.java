package nz.co.searchwellington.feeds.rss;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.feeds.FeedNewsitemCache;
import nz.co.searchwellington.feeds.LiveRssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedImpl;
import nz.co.searchwellington.repositories.ResourceRepository;

public class RssPrefetcherTest extends TestCase {
	
	List<Feed> feeds;
	
	ResourceRepository resourceDAO = mock(ResourceRepository.class);
	LiveRssfeedNewsitemService rssHttpFetcher = mock(LiveRssfeedNewsitemService.class);
	FeedNewsitemCache rssCache = mock(FeedNewsitemCache.class);
	
	RssNewsitemPrefetcher prefetcher;
	
	@Override
	protected void setUp() throws Exception {
		 prefetcher = new RssNewsitemPrefetcher(resourceDAO, rssHttpFetcher, rssCache, null, null);			
		 Feed firstFeed = new FeedImpl();
		 firstFeed.setUrl("http://testdata/rss/1");		
		 Feed secondFeed = new FeedImpl();
		 secondFeed.setUrl("http://testdata/rss/2");
	
		 feeds = new ArrayList<Feed>();
		 feeds.add(firstFeed);
		 feeds.add(secondFeed);
		 stub(resourceDAO.getAllFeeds()).toReturn(feeds);
	}
	
	public void testShouldLoadListOfAllFeedsToPrefetch() throws Exception {		
		prefetcher.run();
		verify(resourceDAO).getAllFeeds();
	}
		
}
