package nz.co.searchwellington.repositories;

import static org.junit.Assert.assertEquals;

import java.util.List;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Suggestion;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class AvailableSuggestedFeeditemsServiceTest {

	private static final String FEED_ITEM_URL_4 = "http://4";
	private static final String FEED_ITEM_URL_3 = "http://3";
	private static final String FEED_ITEM_URL_2 = "http://2";
	private static final String FEED_ITEM_URL_1 = "http://1";
	
	@Mock RssfeedNewsitemService rssfeedNewsitemService;
	
	private List<Suggestion> allSuggestions;
	@Mock Feed feed;
	@Mock FeedNewsitem feedNewsitemOne;
	@Mock FeedNewsitem feedNewsitemTwo;
	@Mock FeedNewsitem feedNewsitemThree;
	@Mock FeedNewsitem feedNewsitemFour;
	private AvailableSuggestedFeeditemsService service;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		allSuggestions = Lists.newArrayList();
		allSuggestions.add(new Suggestion(feed, FEED_ITEM_URL_1, new DateTime().toDate()));
		allSuggestions.add(new Suggestion(feed, FEED_ITEM_URL_2, new DateTime().toDate()));
		allSuggestions.add(new Suggestion(feed, FEED_ITEM_URL_3, new DateTime().toDate()));
		allSuggestions.add(new Suggestion(feed, FEED_ITEM_URL_4, new DateTime().toDate()));
		
		Mockito.when(rssfeedNewsitemService.getFeedNewsitemByUrl(feed, FEED_ITEM_URL_1)).thenReturn(feedNewsitemOne);
		Mockito.when(rssfeedNewsitemService.getFeedNewsitemByUrl(feed, FEED_ITEM_URL_2)).thenReturn(feedNewsitemTwo);
		Mockito.when(rssfeedNewsitemService.getFeedNewsitemByUrl(feed, FEED_ITEM_URL_3)).thenReturn(feedNewsitemThree);
		Mockito.when(rssfeedNewsitemService.getFeedNewsitemByUrl(feed, FEED_ITEM_URL_4)).thenReturn(feedNewsitemFour);
		
		service = new AvailableSuggestedFeeditemsService(rssfeedNewsitemService);
	}
	
	@Test
	public void shouldReturnSuggestedFeeditemsWhichAreCurrentlyAvaileableTrimmedToMaxItems() throws Exception {		
		List<FeedNewsitem> suggestionFeednewsitems = service.getAvailableSuggestedFeeditems(allSuggestions, 3);
		assertEquals(3, suggestionFeednewsitems.size());
	}
	
	@Test
	public void shouldOmitSuggestionsIfTheFeedItemIsNoLongerAvailableIfTheFeedItemCache() throws Exception {
		Mockito.when(rssfeedNewsitemService.getFeedNewsitemByUrl(feed, FEED_ITEM_URL_1)).thenReturn(null);
		Mockito.when(rssfeedNewsitemService.getFeedNewsitemByUrl(feed, FEED_ITEM_URL_2)).thenReturn(null);
		List<FeedNewsitem> suggestionFeednewsitems = service.getAvailableSuggestedFeeditems(allSuggestions, 3);
		assertEquals(2, suggestionFeednewsitems.size());
	}
	
}
