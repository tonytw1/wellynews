package nz.co.searchwellington.urls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class UrlBuilderTest {

	private static final String SITE_URL = "http://siteurl.test";
	
	@Mock SiteInformation siteInformation;

	private UrlBuilder urlBuilder;
	private FrontendNewsitemImpl frontendNewsitem;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(siteInformation.getUrl()).thenReturn(SITE_URL);
		urlBuilder = new UrlBuilder(siteInformation);		

		frontendNewsitem = new FrontendNewsitemImpl();
		frontendNewsitem.setName("Quick brown fox jumps over lazy dog");
		frontendNewsitem.setDate(new DateTime(2010, 10, 12, 0, 0, 0, 0).toDate());
	}
	
	@Test
	public void canContstructPageUrlForFrontendNewsitem() throws Exception {		
		assertNull(frontendNewsitem.getPublisherName());		
		assertEquals(SITE_URL + "/2010/oct/12/quick-brown-fox-jumps-over-lazy-dog", urlBuilder.getTaggingUrl(frontendNewsitem));
	}
	
	@Test
	public void shouldPrefixPageUrlWithPublisherWordsForUrlIfNewsitemHasPublisherSet() throws Exception {
		frontendNewsitem.setPublisherName("Local sports club");
		assertNotNull(frontendNewsitem.getPublisherName());
		assertEquals(SITE_URL + "/local-sports-club/2010/oct/12/quick-brown-fox-jumps-over-lazy-dog", urlBuilder.getTaggingUrl(frontendNewsitem));
	}
	
}
