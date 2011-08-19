package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.urls.UrlBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class PublisherModelBuilderTest {

	@Mock RssUrlBuilder rssUrlBuilder;
	@Mock UrlBuilder urlBuilder;
	@Mock RelatedTagsService relatedTagsService;
	@Mock ContentRetrievalService contentRetrievalService;

	@Mock Website publisher;
	@Mock FrontendNewsitem newsitem;
	@Mock FrontendNewsitem geotaggedNewsitem;
	@Mock Geocode geotag;
	
	MockHttpServletRequest request;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		request.setAttribute("publisher", publisher);
		Mockito.when(geotag.isValid()).thenReturn(true);
		Mockito.when(geotaggedNewsitem.getGeocode()).thenReturn(geotag);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldHightlightPublishersGeotaggedContent() throws Exception {
		PublisherModelBuilder modelBuilder = new PublisherModelBuilder(rssUrlBuilder, relatedTagsService, contentRetrievalService, urlBuilder);
		
		ModelAndView mv = new ModelAndView();
		
		List<FrontendNewsitem> publisherNewsitems = new ArrayList<FrontendNewsitem>();
		publisherNewsitems.add(newsitem);
		publisherNewsitems.add(geotaggedNewsitem);
		
		mv.addObject("main_content", publisherNewsitems);		
		modelBuilder.populateExtraModelConent(request, mv);
		
		List<FrontendNewsitem> geotaggedPublisherNewsitems = (List<FrontendNewsitem>) mv.getModel().get("geocoded");
		assertEquals(1, geotaggedPublisherNewsitems.size());
		assertEquals(geotaggedNewsitem, geotaggedPublisherNewsitems.get(0));
	}
	
}
