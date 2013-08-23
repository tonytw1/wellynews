package nz.co.searchwellington.controllers.admin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendWebsite;
import nz.co.searchwellington.urls.UrlBuilder;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AdminUrlBuilderTest {

	private static final String SITE_URL = "http://somesite.local";
	
	@Mock SiteInformation siteInformation;
	
	private FrontendWebsite frontendWebsite;
	private FrontendFeed frontendFeed;
	private FrontendNewsitem frontendNewsitem;
	
	private AdminUrlBuilder adminUrlBuilder;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(siteInformation.getUrl()).thenReturn(SITE_URL);
		adminUrlBuilder = new AdminUrlBuilder(siteInformation, new UrlBuilder(siteInformation, null, new UrlWordsGenerator()));
		
		frontendWebsite = new FrontendWebsite();
		frontendWebsite.setId(123);
		frontendWebsite.setType("W");
		frontendWebsite.setName("My local sports team");
		frontendWebsite.setUrlWords("my-local-sports-team");
		
		frontendNewsitem = new FrontendNewsitem();
		frontendNewsitem.setId(123);
		frontendNewsitem.setType("N");
		frontendNewsitem.setName("A news item");
		frontendNewsitem.setPublisherName("My local sports team");
		frontendNewsitem.setDate(new DateTime(2011, 5, 20, 0, 0, 0, 0).toDate());
		
		frontendFeed = new FrontendFeed();
		frontendFeed.setId(124);
		frontendFeed.setType("F");
		frontendFeed.setUrlWords("my-local-sports-team-news");
	}

	@Test
	public void canConstructEditUrlForFrontendWebsite() throws Exception {
		assertEquals("http://somesite.local/my-local-sports-team/edit", adminUrlBuilder.getResourceEditUrl(frontendWebsite));
	}
	
	@Test
	public void canBuildEditUrlForNewsitems() throws Exception {
		//assertEquals("http://somesite.local/my-local-sports-team/2011/may/20/a-news-item/edit", adminUrlBuilder.getResourceEditUrl(frontendNewsitem));
		assertEquals("http://somesite.local/edit/edit?resource=123", adminUrlBuilder.getResourceEditUrl(frontendNewsitem));

	}
	
	@Test
	public void canConstructEditUrlForFrontendFeed() throws Exception {
		assertEquals("http://somesite.local/feed/my-local-sports-team-news/edit", adminUrlBuilder.getResourceEditUrl(frontendFeed));
	}
	
	@Test
	public void canConstructDeleteUrlForFrontendResource() throws Exception {
		assertEquals("http://somesite.local/my-local-sports-team/delete", adminUrlBuilder.getResourceDeleteUrl(frontendWebsite));
	}
	
	@Test
	public void canConstructCheckUrlForFrontendResource() throws Exception {
		assertEquals("http://somesite.local/admin/linkchecker/add?resource=123",adminUrlBuilder.getResourceCheckUrl(frontendWebsite));
	}
	
	@Test
	public void canConstructViewSnapshotUrlForFrontendResource() throws Exception {
		assertEquals("http://somesite.local/my-local-sports-team/viewsnapshot",adminUrlBuilder.getViewSnapshotUrl(frontendWebsite));
	}
	
	@Test
	public void canConstructAutoGatherUrlForPublisher() throws Exception {
		assertEquals("http://somesite.local/my-local-sports-team/gather", adminUrlBuilder.getPublisherAutoGatherUrl(frontendWebsite));
	}
	
}
