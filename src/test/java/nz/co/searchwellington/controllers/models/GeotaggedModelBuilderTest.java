package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.filters.LocationParameterFilter;
import nz.co.searchwellington.model.Geocode;
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

public class GeotaggedModelBuilderTest {
	
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock UrlBuilder urlBuilder;
	@Mock RssUrlBuilder rssUrlBuilder;
	@Mock List<FrontendResource> newsitemsNearPetoneStation;
	
	private MockHttpServletRequest request;
	private Geocode validLocation;
	@Mock Geocode invalidLocation;
	
	private ModelBuilder modelBuilder;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();		
		validLocation = new Geocode("Petone Station", 1, 2);
		modelBuilder = new GeotaggedModelBuilder(contentRetrievalService, urlBuilder, rssUrlBuilder);
	}
	
	@Test
	public void testShouldBeValidForTagCommentPath() throws Exception {
		request.setPathInfo("/geotagged");
		assertTrue(modelBuilder.isValid(request));
	}
	
	@Test
	public void testShouldBeValidForTagCommentRssPath() throws Exception {		
		request.setPathInfo("/geotagged/rss");
		assertTrue(modelBuilder.isValid(request));
	}
	
	@Test
	public void testShouldBeValidForTagCommentJSONPath() throws Exception {		
		request.setPathInfo("/geotagged/json");
		assertTrue(modelBuilder.isValid(request));
	}
	
	@Test
	public void locationSearchesShouldHaveNearbyNewsitemsAsTheMainContent() throws Exception {
		Mockito.when(contentRetrievalService.getNewsitemsNear(1, 2, 2)).thenReturn(newsitemsNearPetoneStation);
		request.setPathInfo("/geotagged");
		request.setAttribute(LocationParameterFilter.LOCATION, validLocation);
		
		ModelAndView modelAndView = modelBuilder.populateContentModel(request, false);
		
		assertEquals(newsitemsNearPetoneStation, modelAndView.getModel().get("main_content"));
	}
	
	@Test
	public void locationSearchShouldNotSetMainContentIfTheLocationWasInvalid() throws Exception {
		request.setPathInfo("/geotagged");
		Mockito.when(invalidLocation.isValid()).thenReturn(false);
		request.setAttribute(LocationParameterFilter.LOCATION, invalidLocation);
				
		ModelAndView modelAndView = modelBuilder.populateContentModel(request, false);

		assertNull(modelAndView.getModel().get("main_content"));
	}
	
}
