package nz.co.searchwellington.controllers.admin;

import static org.junit.Assert.assertEquals;
import nz.co.searchwellington.model.FrontEndWebsite;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;
import nz.co.searchwellington.model.frontend.FrontendResourceImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AdminUrlBuilderTest {

	private static final String SITE_URL = "http://somesite.local";
	
	@Mock SiteInformation siteInformation;
	@Mock FrontEndWebsite frontendWebsite;
	private FrontendResourceImpl frontendResource;
	
	private AdminUrlBuilder adminUrlBuilder;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(siteInformation.getUrl()).thenReturn(SITE_URL);
		Mockito.when(frontendWebsite.getUrlWords()).thenReturn("my-local-sports-team");
		frontendResource = new FrontendNewsitemImpl();
		frontendResource.setId(123);
		adminUrlBuilder = new AdminUrlBuilder(siteInformation);		
	}

	@Test
	public void canConstructEditUrlForFrontendResource() throws Exception {
		assertEquals("http://somesite.local/edit/edit?resource=123",adminUrlBuilder.getResourceEditUrl(frontendResource));
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
		assertEquals("http://somesite.local/admin/gather?publisher=my-local-sports-team", adminUrlBuilder.getPublisherAutoGatherUrl(frontendWebsite));
	}
	
}
