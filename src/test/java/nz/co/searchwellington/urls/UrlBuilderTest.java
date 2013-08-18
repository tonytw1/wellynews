package nz.co.searchwellington.urls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendFeedImpl;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;
import nz.co.searchwellington.twitter.CachingTwitterService;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.Place;

public class UrlBuilderTest {

	private static final String SITE_URL = "http://siteurl.test";
	
	@Mock SiteInformation siteInformation;
	@Mock CachingTwitterService twitterService;
	
	private UrlBuilder urlBuilder;
	private FrontendNewsitemImpl frontendNewsitem;
	private FrontendFeed frontendFeed;
	private Tag tag;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(siteInformation.getUrl()).thenReturn(SITE_URL);
		urlBuilder = new UrlBuilder(siteInformation, twitterService);		

		frontendNewsitem = new FrontendNewsitemImpl();
		frontendNewsitem.setName("Quick brown fox jumps over lazy dog");
		frontendNewsitem.setDate(new DateTime(2010, 10, 12, 0, 0, 0, 0).toDate());
		frontendNewsitem.setUrlWords("/2010/oct/12/quick-brown-fox-jumps-over-lazy-dog");
		frontendFeed = new FrontendFeedImpl();
		
		tag = new Tag();
		tag.setName("atag");
	}
	
	@Test
	public void testTagSearchRefinementsShouldBeOnTheTagPages() throws Exception {
		assertEquals(SITE_URL + "/atag?keywords=something", urlBuilder.getTagSearchUrl(tag, "something"));
	}
	
	@Test
	public void canCreatePublisherAndTagCombinerLinkBasedOnPublisherUrlWordsAndTagName() throws Exception {
		assertEquals(SITE_URL + "/wellington-city-council+atag", urlBuilder.getPublisherCombinerUrl("Wellington City Council", tag));
	}
	
	@Test
	public void canCreateLocationSearchUrlFromGeotag() throws Exception {
		final Place somewhere = new Place("Somewhere,Far away", new LatLong(3.1, 4.2), null);
		assertEquals(SITE_URL + "/geotagged?location=Somewhere%2CFar+away", urlBuilder.getLocationUrlFor(somewhere));
	}
	
	@Test
	public void locationsShouldBeLinkedByOSMIdIfAvailable() throws Exception {
		final Place somewhereWithOSMid= new Place("Somewhere,Far away", new LatLong(3.1, 4.2), new OsmId(12345, "N"));
		assertEquals("http://siteurl.test/geotagged?osm=12345%2FN", urlBuilder.getLocationUrlFor(somewhereWithOSMid));
	}
	
	@Test
	public void shouldConstructPageUrlForFrontendResourceFromResourcesUrlWords() throws Exception {		
		assertNull(frontendNewsitem.getPublisherName());		
		assertEquals(SITE_URL + "/2010/oct/12/quick-brown-fox-jumps-over-lazy-dog", urlBuilder.getLocalPageUrl(frontendNewsitem));
	}
	
	@Test
	public void canGenerateFrontendPublisherPageUrl() throws Exception {
		assertEquals(SITE_URL + "/wellington-city-council", urlBuilder.getPublisherUrl("Wellington City Council"));
	}

	@Test
	public void canGenerateTwitterReactionsPageUrl() throws Exception {
		assertEquals(SITE_URL + "/twitter", urlBuilder.getTwitterReactionsUrl());
	}
	
	@Test
	public void urlForFeedsShouldPointToOurFeedPage() throws Exception {
		frontendFeed.setUrlWords("my-local-sports-team-match-reports");
		assertEquals(SITE_URL + "/feed/my-local-sports-team-match-reports", urlBuilder.getFeedUrl(frontendFeed));
	}
	
}
