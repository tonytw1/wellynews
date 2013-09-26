package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.mappers.FrontendResourceMapper;
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
	@Mock GeotaggedNewsitemExtractor geotaggedNewsitemExtractor;
	@Mock FeedItemLocalCopyDecorator feedItemLocalCopyDecorator;
	@Mock FrontendResourceMapper frontendResourceMapper;
	
	@Mock Feed feed;
	@Mock List<FrontendFeedNewsitem> feedNewsitems;
	@Mock List<FrontendFeedNewsitem> feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation;
	@Mock List<FrontendNewsitem> geotaggedFeedNewsitems;
	@Mock FrontendResource frontendFeed;
	
	MockHttpServletRequest request;
	ModelBuilder modelBuilder;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);		
		when(rssfeedNewsitemService.getFeedNewsitems(feed)).thenReturn(feedNewsitems);
		when(feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(feedNewsitems)).thenReturn(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation);
		when(feedNewsitems.size()).thenReturn(10);
		
		request = new MockHttpServletRequest();
		request.setAttribute("feedAttribute", feed);
		request.setPathInfo("/feed/someonesfeed");
		
		modelBuilder = new FeedModelBuilder(rssfeedNewsitemService, contentRetrievalService, geotaggedNewsitemExtractor, feedItemLocalCopyDecorator, frontendResourceMapper);
	}
	
	@Test
	public void feedPathsAreValid() throws Exception {
		assertTrue(modelBuilder.isValid(request));
	}
	
	@Test
	public void shouldPopulateFrontendFeedFromRequestAttribute() throws Exception {
		when(frontendResourceMapper.createFrontendResourceFrom(feed)).thenReturn(frontendFeed);
		ModelAndView mv = modelBuilder.populateContentModel(request);	
		assertEquals(frontendFeed, mv.getModel().get("feed"));
	}
	
	@Test
	public void shouldPopulateMainContentWithFeedItemsDecoratedWithLocalCopySuppressionInformation() throws Exception {		
		ModelAndView mv = modelBuilder.populateContentModel(request);
		assertEquals(feedNewsitemsDecoratedWithLocalCopyAndSuppressionInformation, mv.getModel().get("main_content"));
	}
	
	@Test
	public void shouldPushGeotaggedFeeditemsOntoTheModelSeperately() throws Exception {
		when(geotaggedNewsitemExtractor.extractGeotaggedItemsFromFeedNewsitems(feedNewsitems)).thenReturn(geotaggedFeedNewsitems);		
		ModelAndView mv = modelBuilder.populateContentModel(request);
		modelBuilder.populateExtraModelContent(request, mv);
		assertEquals(geotaggedFeedNewsitems, mv.getModel().get("geocoded"));
	}
	
}
