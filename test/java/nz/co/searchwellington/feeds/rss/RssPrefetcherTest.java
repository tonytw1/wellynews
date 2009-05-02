package nz.co.searchwellington.feeds.rss;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedImpl;
import nz.co.searchwellington.repositories.ResourceRepository;

import com.sun.syndication.feed.synd.SyndFeed;

public class RssPrefetcherTest extends TestCase {
	
	List<Feed> feeds;
	
	ResourceRepository resourceDAO = mock(ResourceRepository.class);
	RssHttpFetcher rssHttpFetcher = mock(RssHttpFetcher.class);
	RssCache rssCache = mock(RssCache.class);
	
	RssNewsitemPrefetcher prefetcher;
	
	@Override
	protected void setUp() throws Exception {
		 prefetcher = new RssNewsitemPrefetcher(resourceDAO, rssHttpFetcher, rssCache);
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
	
	public void testShouldFetchEachFeed() throws Exception {		
		prefetcher.run();		
		verify(rssHttpFetcher).httpFetch("http://testdata/rss/1");
		verify(rssHttpFetcher).httpFetch("http://testdata/rss/2");
	}
	
	public void testShouldPutSuccessfulLoaedFeedsIntoTheCache() throws Exception {
		SyndFeed firstSyndFeed = mock(SyndFeed.class);		
		stub(rssHttpFetcher.httpFetch("http://testdata/rss/1")).toReturn(firstSyndFeed);
		stub(rssHttpFetcher.httpFetch("http://testdata/rss/2")).toReturn(null);		
		prefetcher.run();	
		verify(rssCache).putFeedIntoCache("http://testdata/rss/1", firstSyndFeed);
	}
	
}
