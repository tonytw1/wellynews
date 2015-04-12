package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder;
import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.flickr.FlickrService;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;
import nz.co.searchwellington.views.GeocodeToPlaceMapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class TagModelBuilderTest {

	private static final String TAG_DISPLAY_NAME = "Penguins";

	@Mock ContentRetrievalService contentRetrievalService;
	@Mock RssUrlBuilder rssUrlBuilder;
	@Mock UrlBuilder urlBuilder;
	@Mock RelatedTagsService relatedTagsService;
	@Mock RssfeedNewsitemService rssfeedNewsitemService;
	@Mock FlickrService flickrService;
	@Mock FeedItemLocalCopyDecorator feedItemLocalCopyDecorator;
	@Mock GeocodeToPlaceMapper geocodeToPlaceMapper;
    @Mock CommonAttributesModelBuilder commonAttributesModelBuilder;
	
	@Mock List<FrontendResource> tagNewsitems;	
	@Mock Tag tag;
	MockHttpServletRequest  request;

	private TagModelBuilder modelBuilder;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		modelBuilder = new TagModelBuilder(rssUrlBuilder, urlBuilder,
				relatedTagsService, rssfeedNewsitemService,
				contentRetrievalService, flickrService, feedItemLocalCopyDecorator, geocodeToPlaceMapper, commonAttributesModelBuilder);
		request = new MockHttpServletRequest();
		Mockito.when(tag.getDisplayName()).thenReturn(TAG_DISPLAY_NAME);
	}
	
	@Test
	public void isNotValidIfNotTagsAreOnTheRequest() throws Exception {
		assertFalse(modelBuilder.isValid(request));
	}
	
	@Test
	public void isValidIsOneTagIsOnTheRequest() throws Exception {
		request.setAttribute("tags", Arrays.asList(tag));
		assertTrue(modelBuilder.isValid(request));
	}
	
	@Test
	public void isNotValidIfMoreThanOneTagIsOnTheRequest() throws Exception {
		request.setAttribute("tags", Arrays.asList(tag, tag));
		assertFalse(modelBuilder.isValid(request));
	}
	
	@Test
	public void tagPageHeadingShouldBeTheTagDisplayName() throws Exception {
		request.setAttribute("tags", Arrays.asList(tag));
		ModelAndView mv = modelBuilder.populateContentModel(request);
		assertEquals(TAG_DISPLAY_NAME, mv.getModel().get("heading"));
	}
	
	@Test
	public void mainContentShouldBeTagNewsitems() throws Exception {
		request.setAttribute("tags", Arrays.asList(tag));
		Mockito.when(contentRetrievalService.getTaggedNewsitems(tag, 0, 30)).thenReturn(tagNewsitems);
		ModelAndView mv = modelBuilder.populateContentModel(request);
		
		assertEquals(tagNewsitems, mv.getModel().get("main_content"));
	}
	
}
