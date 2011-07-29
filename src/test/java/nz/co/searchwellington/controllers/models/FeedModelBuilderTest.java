package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class FeedModelBuilderTest {
	
	@Mock RssfeedNewsitemService rssfeedNewsitemService;
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock Feed feed;
	@Mock List<FeedNewsitem> feedNewsitems;
	@Mock List<FrontendFeedNewsitem> feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation;
	@Mock List<FrontendFeedNewsitem> geotaggedFeedNewsitems;
	
	MockHttpServletRequest request;
	ModelBuilder modelBuilder;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);		
		when(contentRetrievalService.getFeedByUrlWord("someonesfeed")).thenReturn(feed);
		when(rssfeedNewsitemService.getFeedNewsitems(feed)).thenReturn(feedNewsitems);
		when(rssfeedNewsitemService.addSupressionAndLocalCopyInformation(feedNewsitems)).thenReturn(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation);
		when(feedNewsitems.size()).thenReturn(10);
		request = new MockHttpServletRequest();
		request.setPathInfo("/feed/someonesfeed");
		
		modelBuilder = new FeedModelBuilder(rssfeedNewsitemService, contentRetrievalService);
	}
	
	@Test
	public void feedPathsAreValid() throws Exception {
		assertTrue(modelBuilder.isValid(request));
	}
	
	@Test
	public void shouldPopulateFeedFromUrlWordsOnRequestPath() throws Exception {
		ModelAndView mv = modelBuilder.populateContentModel(request);
		verify(contentRetrievalService).getFeedByUrlWord("someonesfeed");
		assertEquals(feed, mv.getModel().get("feed"));
	}
	
	@Test
	public void shouldPopulateMainContentWithFeedItemsDecoratedWithLocalCopySuppressionInformation() throws Exception {		
		ModelAndView mv = modelBuilder.populateContentModel(request);
		assertEquals(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation, mv.getModel().get("main_content"));
	}

	@Test
	public void shouldPushGeotaggedFeeditemsOntoTheModelSeperately() throws Exception {
		when(rssfeedNewsitemService.extractGeotaggedFeeditems(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation)).thenReturn(geotaggedFeedNewsitems);		
		ModelAndView mv = modelBuilder.populateContentModel(request);
		modelBuilder.populateExtraModelConent(request, false, mv);
		assertEquals(geotaggedFeedNewsitems, mv.getModel().get("geocoded"));
	}
	
}
