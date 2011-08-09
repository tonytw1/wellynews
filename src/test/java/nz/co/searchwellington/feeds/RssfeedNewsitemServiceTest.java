package nz.co.searchwellington.feeds;

import static org.junit.Assert.assertEquals;
import nz.co.searchwellington.feeds.rss.RssHttpFetcher;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.utils.TextTrimmer;
import nz.co.searchwellington.utils.UrlCleaner;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RssfeedNewsitemServiceTest {

	@Mock Geocode geocode;
	@Mock private Feed feed;
	
	@Mock UrlCleaner urlCleaner;
	@Mock RssHttpFetcher rssFetcher;
	@Mock TextTrimmer textTrimmer;
	
	RssfeedNewsitemService rssfeedNewsitemService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		rssfeedNewsitemService = new LiveRssfeedNewsitemService(urlCleaner, rssFetcher, textTrimmer);
	}
		
	@Test
	// TODO This method under test probably wants to be it's own service (ie. newing up one of the abstract classes extenders to get at this method is weird)
	public void shouldSetGeocodeWhenAcceptingFeedNewsitem() throws Exception {		
		FeedNewsitem feedNewsitem = new FeedNewsitem();
		feedNewsitem.setGeocode(geocode);
		
		Newsitem newsitem = rssfeedNewsitemService.makeNewsitemFromFeedItem(feed, feedNewsitem);
		
		assertEquals(geocode, newsitem.getGeocode());
	}
	
}
