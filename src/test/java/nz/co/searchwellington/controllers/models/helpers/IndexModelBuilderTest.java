package nz.co.searchwellington.controllers.models.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.models.helpers.ArchiveLinksService;
import nz.co.searchwellington.controllers.models.helpers.CommonAttributesModelBuilder;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class IndexModelBuilderTest {

	@Mock ContentRetrievalService contentRetrievalService;
	@Mock RssUrlBuilder rssUrlBuilder;
	@Mock LoggedInUserFilter loggedInUserFilter;
	@Mock UrlBuilder urlBuilder;
	@Mock ArchiveLinksService archiveLinksService;
    @Mock CommonAttributesModelBuilder commonAttributesModelBuilder;

	MockHttpServletRequest request;

	@Mock List<FrontendResource> latestNewsitems;
	
	private IndexModelBuilder modelBuilder;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		modelBuilder = new IndexModelBuilder(contentRetrievalService, rssUrlBuilder, loggedInUserFilter, urlBuilder, archiveLinksService, commonAttributesModelBuilder);
		request = new MockHttpServletRequest();
		request.setPathInfo("/");
	}
	
	@Test
	public void isValidForHomePageUrl() throws Exception {
		assertTrue(modelBuilder.isValid(request));
	}
	
	@Test
	public void isNotValidForMainRssUrlAsThatsTakenCareOfByFeedBurner() throws Exception {
		request.setPathInfo("/rss");
		assertFalse(modelBuilder.isValid(request));
	}
	
	@Test
	public void isValidForMainJsonUrl() throws Exception {
		request.setPathInfo("/json");
		assertTrue(modelBuilder.isValid(request));
	}
	
	@Test
	public void indexPageMainContentIsTheLatestNewsitems() throws Exception {
		Mockito.when(contentRetrievalService.getLatestNewsitems(30)).thenReturn(latestNewsitems);
		
		ModelAndView mv = modelBuilder.populateContentModel(request);
		
		assertEquals(latestNewsitems, mv.getModel().get("main_content"));		
	}
	
}
