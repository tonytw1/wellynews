package nz.co.searchwellington.urls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.frontend.FrontendFeedImpl;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;
import nz.co.searchwellington.twitter.TwitterService;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class UrlBuilderTest {

	private static final String SITE_URL = "http://siteurl.test";
	
	@Mock SiteInformation siteInformation;
	@Mock TwitterService twitterService;
	@Mock DateFormatter dateFormatter;

	private UrlBuilder urlBuilder;
	private FrontendNewsitemImpl frontendNewsitem;
	private FrontendFeedImpl frontendFeed;
	private Tag tag;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(siteInformation.getUrl()).thenReturn(SITE_URL);
		urlBuilder = new UrlBuilder(siteInformation, twitterService, dateFormatter);		

		frontendNewsitem = new FrontendNewsitemImpl();
		frontendNewsitem.setName("Quick brown fox jumps over lazy dog");
		frontendNewsitem.setDate(new DateTime(2010, 10, 12, 0, 0, 0, 0).toDate());
		
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
		Geocode somewhere = new Geocode("Somewhere,Far away", 3.1, 4.2);
		assertEquals(SITE_URL + "/geotagged?location=Somewhere%2CFar+away", urlBuilder.getLocationUrlFor(somewhere));
	}
	
	@Test
	public void canContstructPageUrlForFrontendNewsitem() throws Exception {		
		assertNull(frontendNewsitem.getPublisherName());		
		assertEquals(SITE_URL + "/2010/oct/12/quick-brown-fox-jumps-over-lazy-dog", urlBuilder.getLocalPageUrl(frontendNewsitem));
	}
	
	@Test
	public void shouldPrefixPageUrlWithPublisherWordsForUrlIfNewsitemHasPublisherSet() throws Exception {
		frontendNewsitem.setPublisherName("Local sports club");
		assertEquals(SITE_URL + "/local-sports-club/2010/oct/12/quick-brown-fox-jumps-over-lazy-dog", urlBuilder.getLocalPageUrl(frontendNewsitem));
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
