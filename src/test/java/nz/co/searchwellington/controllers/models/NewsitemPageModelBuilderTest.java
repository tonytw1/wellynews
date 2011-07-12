package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.GeotaggingVote;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService;
import nz.co.searchwellington.widgets.TagWidgetFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class NewsitemPageModelBuilderTest {
	
	private static final int NEWSITEM_ID = 123;
	
	private static final String VALID_NEWSITEM_PAGE_PATH = "/wellington-city-council/2010/feb/01/something-about-rates";
	
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock TaggingReturnsOfficerService taggingReturnsOfficerService;
	@Mock TagWidgetFactory tagWidgetFactory;
	@Mock HandTaggingDAO handTaggingDAO;
	@Mock LoggedInUserFilter loggedInUserFilter;
	@Mock Newsitem geotaggedNewsitem;
	@Mock Newsitem newsitem;
	@Mock Geocode validGeotag;
	@Mock ResourceRepository resourceDAO;
	@Mock List<GeotaggingVote> geotagVotes;
	
	private MockHttpServletRequest request;	
	private ModelBuilder builder;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		builder = new NewsitemPageModelBuilder(contentRetrievalService, taggingReturnsOfficerService, tagWidgetFactory, handTaggingDAO, loggedInUserFilter, resourceDAO);
		request = new MockHttpServletRequest();
		request.setPathInfo(VALID_NEWSITEM_PAGE_PATH);
		when(newsitem.getId()).thenReturn(123);
	}
	
	@Test
	public void shouldAcceptValidFormatPath() throws Exception {
		assertTrue(builder.isValid(request));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldPopulateGeotaggedItemsWithNewsitemIfHasValidGeotag() throws Exception {
		when(validGeotag.isValid()).thenReturn(true);
		when(geotaggedNewsitem.getGeocode()).thenReturn(validGeotag);
		when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(geotaggedNewsitem);

		ModelAndView mv = builder.populateContentModel(request, false);
		
		List<Resource> geotagged = (List<Resource>) mv.getModel().get("geocoded");
		assertEquals(1, geotagged.size());
		assertEquals(geotaggedNewsitem, geotagged.get(0));
	}
	
	@Test
	public void shouldNotPopulateGeotaggedItemsIfNewsitemIsNotGeotagged() throws Exception {
		when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(newsitem);
		ModelAndView mv = builder.populateContentModel(request, false);
		assertNull(mv.getModel().get("geocoded"));		
	}
	
	@Test
	public void shouldDisplayGeotaggingVotes() throws Exception {
		when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(newsitem);
		when(resourceDAO.loadResourceById(NEWSITEM_ID)).thenReturn(newsitem);

		when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(geotagVotes);

		ModelAndView mv = builder.populateContentModel(request, false);
		
		assertEquals(geotagVotes, mv.getModel().get("geotag_votes"));
	}
	
}
