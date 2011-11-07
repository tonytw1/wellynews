package nz.co.searchwellington.controllers.admin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.frontend.FrontendWebsite;
import nz.co.searchwellington.model.frontend.FrontendWebsiteImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AdminUrlBuilderTest {

	private static final String SITE_URL = "http://somesite.local";
	
	@Mock SiteInformation siteInformation;
	@Mock FrontendWebsite frontendWebsite;
	private FrontendWebsiteImpl frontendResource;
	
	private AdminUrlBuilder adminUrlBuilder;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(siteInformation.getUrl()).thenReturn(SITE_URL);
		adminUrlBuilder = new AdminUrlBuilder(siteInformation);
		
		frontendResource = new FrontendWebsiteImpl();
		frontendResource.setId(123);
		frontendResource.setType("W");
		frontendResource.setUrlWords("my-local-sports-team");
	}

	@Test
	public void canConstructEditUrlForFrontendWebsite() throws Exception {
		assertEquals("http://somesite.local/edit/edit?resource=my-local-sports-team",adminUrlBuilder.getResourceEditUrl(frontendResource));
	}
	
	@Test
	public void canConstructDeleteUrlForFrontendResource() throws Exception {
		assertEquals("http://somesite.local/edit/delete?resource=123",adminUrlBuilder.getResourceDeleteUrl(frontendResource));
	}
	
	@Test
	public void canConstructCheckUrlForFrontendResource() throws Exception {
		assertEquals("http://somesite.local/admin/linkchecker/add?resource=123",adminUrlBuilder.getResourceCheckUrl(frontendResource));
	}
	
	@Test
	public void canConstructViewSnapshotUrlForFrontendResource() throws Exception {
		assertEquals("http://somesite.local/edit/viewsnapshot?resource=123",adminUrlBuilder.getViewSnapshotUrl(frontendResource));
	}
	
	@Test
	public void canConstructAutoGatherUrlForPublisher() throws Exception {
		assertEquals("http://somesite.local/admin/gather?publisher=my-local-sports-team", adminUrlBuilder.getPublisherAutoGatherUrl(frontendResource));
	}
	
}
