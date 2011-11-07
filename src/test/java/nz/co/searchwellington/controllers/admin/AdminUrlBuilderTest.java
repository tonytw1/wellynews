package nz.co.searchwellington.controllers.admin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.frontend.FrontendFeedImpl;
import nz.co.searchwellington.model.frontend.FrontendWebsiteImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AdminUrlBuilderTest {

	private static final String SITE_URL = "http://somesite.local";
	
	@Mock SiteInformation siteInformation;
	
	private FrontendWebsiteImpl frontendWebsite;
	private FrontendFeedImpl frontendFeed;
	
	private AdminUrlBuilder adminUrlBuilder;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(siteInformation.getUrl()).thenReturn(SITE_URL);
		adminUrlBuilder = new AdminUrlBuilder(siteInformation);
		
		frontendWebsite = new FrontendWebsiteImpl();
		frontendWebsite.setId(123);
		frontendWebsite.setType("W");
		frontendWebsite.setUrlWords("my-local-sports-team");
		
		frontendFeed = new FrontendFeedImpl();
		frontendFeed.setId(124);
		frontendFeed.setType("F");
		frontendFeed.setName("My local sports team news");
	}

	@Test
	public void canConstructEditUrlForFrontendWebsite() throws Exception {
		assertEquals("http://somesite.local/edit/edit?resource=my-local-sports-team",adminUrlBuilder.getResourceEditUrl(frontendWebsite));
	}
	
	@Test
	public void canConstructEditUrlForFrontendFeed() throws Exception {
		assertEquals("http://somesite.local/edit/edit?resource=feed/my-local-sports-team-news",adminUrlBuilder.getResourceEditUrl(frontendFeed));
	}
	
	@Test
	public void canConstructDeleteUrlForFrontendResource() throws Exception {
		assertEquals("http://somesite.local/edit/delete?resource=123",adminUrlBuilder.getResourceDeleteUrl(frontendWebsite));
	}
	
	@Test
	public void canConstructCheckUrlForFrontendResource() throws Exception {
		assertEquals("http://somesite.local/admin/linkchecker/add?resource=123",adminUrlBuilder.getResourceCheckUrl(frontendWebsite));
	}
	
	@Test
	public void canConstructViewSnapshotUrlForFrontendResource() throws Exception {
		assertEquals("http://somesite.local/edit/viewsnapshot?resource=123",adminUrlBuilder.getViewSnapshotUrl(frontendWebsite));
	}
	
	@Test
	public void canConstructAutoGatherUrlForPublisher() throws Exception {
		assertEquals("http://somesite.local/admin/gather?publisher=my-local-sports-team", adminUrlBuilder.getPublisherAutoGatherUrl(frontendWebsite));
	}
	
}
