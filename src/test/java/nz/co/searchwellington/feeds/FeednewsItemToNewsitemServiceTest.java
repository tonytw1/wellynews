package nz.co.searchwellington.feeds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FeednewsItemToNewsitemServiceTest {

	@Mock Geocode geocode;
	@Mock private Feed feed;
	
	FeednewsItemToNewsitemService service;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		service = new FeednewsItemToNewsitemService();
	}
	
	@Test
	public void shouldSetGeocodeWhenAcceptingFeedNewsitem() throws Exception {
		fail();
		FeedNewsitem feedNewsitem = new FeedNewsitem();
		//feedNewsitem.setGeocode(geocode);	
		
		Newsitem newsitem = service.makeNewsitemFromFeedItem(feed, feedNewsitem);
		
		assertEquals(geocode, newsitem.getGeocode());
	}
	
}
