package nz.co.searchwellington.controllers.models;

import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

public class GeotaggedModelBuilderTest extends TestCase {

	
	private MockHttpServletRequest request;
	ModelBuilder modelBuilder;
	
	@Override
	protected void setUp() throws Exception {
		request = new MockHttpServletRequest();		
		modelBuilder = new GeotaggedModelBuilder(null, null, null);		
	}
		
	public void testShouldBeValidForTagCommentPath() throws Exception {
		request.setPathInfo("/geotagged");
		assertTrue(modelBuilder.isValid(request));
	}
	
	public void testShouldBeValidForTagCommentRssPath() throws Exception {		
		request.setPathInfo("/geotagged/rss");
		assertTrue(modelBuilder.isValid(request));
	}
	
	public void testShouldBeValidForTagCommentJSONPath() throws Exception {		
		request.setPathInfo("/geotagged/json");
		assertTrue(modelBuilder.isValid(request));
	}
}
