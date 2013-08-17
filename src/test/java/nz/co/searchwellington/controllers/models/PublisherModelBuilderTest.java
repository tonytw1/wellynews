package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertEquals;

import java.util.List;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
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

import uk.co.eelpieconsulting.common.geo.model.Place;

import com.google.common.collect.Lists;

public class PublisherModelBuilderTest {

	@Mock RssUrlBuilder rssUrlBuilder;
	@Mock UrlBuilder urlBuilder;
	@Mock RelatedTagsService relatedTagsService;
	@Mock ContentRetrievalService contentRetrievalService;
	@Mock GeotaggedNewsitemExtractor geotaggedNewsitemExtractor;
	
	@Mock Website publisher;
	@Mock FrontendNewsitem newsitem;
	@Mock FrontendNewsitem geotaggedNewsitem;
	@Mock Place geotag;
	
	MockHttpServletRequest request;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		request.setAttribute("publisher", publisher);
		Mockito.when(geotaggedNewsitem.getPlace()).thenReturn(geotag);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldHightlightPublishersGeotaggedContent() throws Exception {
		final List<FrontendNewsitem> publisherNewsitems = Lists.newArrayList();
		publisherNewsitems.add(newsitem);
		publisherNewsitems.add(geotaggedNewsitem);
				
		final List<FrontendNewsitem> geotaggedNewsitems = Lists.newArrayList();
		geotaggedNewsitems.add(geotaggedNewsitem);

		Mockito.when(geotaggedNewsitemExtractor.extractGeotaggedItems(publisherNewsitems)).thenReturn(geotaggedNewsitems);
		
		PublisherModelBuilder modelBuilder = new PublisherModelBuilder(rssUrlBuilder, relatedTagsService, contentRetrievalService, urlBuilder, geotaggedNewsitemExtractor);
		
		final ModelAndView mv = new ModelAndView();
		
		
		mv.addObject("main_content", publisherNewsitems);		
		modelBuilder.populateExtraModelContent(request, mv);
		
		final List<FrontendResource> geotaggedPublisherNewsitems = (List<FrontendResource>) mv.getModel().get("geocoded");
		assertEquals(geotaggedPublisherNewsitems, geotaggedNewsitems);
		assertEquals(geotaggedNewsitem, geotaggedPublisherNewsitems.get(0));
	}
	
}
