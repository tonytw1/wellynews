package nz.co.searchwellington.feeds;

import org.junit.Test;

import nz.co.searchwellington.feeds.rss.RssHttpFetcher;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedImpl;
import nz.co.searchwellington.urls.UrlResolverService;
import nz.co.searchwellington.utils.TextTrimmer;
import nz.co.searchwellington.utils.UrlCleaner;

public class LiveRssfeedNewsitemServiceTest {

	private UrlCleaner urlCleaner;
	private RssHttpFetcher rssFetcher;
	private TextTrimmer textTrimmer;
	private FeednewsItemToNewsitemService feednewsItemToNewsitemService;
	
	@Test
	public void testname() throws Exception {
		// TODO Disabled test class placeholder
	}
	
	//@Test
	public void test() throws Exception {
		urlCleaner = new UrlCleaner(new UrlResolverService());
		rssFetcher = new RssHttpFetcher();
		textTrimmer = new TextTrimmer();
		
		LiveRssfeedNewsitemService liveRssfeedNewsitemService = new LiveRssfeedNewsitemService(urlCleaner, rssFetcher, textTrimmer, feednewsItemToNewsitemService);
		Feed feed = new FeedImpl();
		feed.setUrl("http://www.capitaltimes.co.nz/page/32/rss/News.html");
		liveRssfeedNewsitemService.getFeedNewsitems(feed);
	}
	
}
