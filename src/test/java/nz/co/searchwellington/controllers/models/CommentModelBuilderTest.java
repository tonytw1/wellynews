package nz.co.searchwellington.controllers.models;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;

public class CommentModelBuilderTest extends TestCase {

	private MockHttpServletRequest request;
	ModelBuilder modelBuilder;
	
	@Override
	protected void setUp() throws Exception {
		request = new MockHttpServletRequest();		
		modelBuilder = new CommentModelBuilder(null, null, null);
	}
		
	public void testShouldBeValidForTagCommentPath() throws Exception {
		request.setPathInfo("/comment");
		assertTrue(modelBuilder.isValid(request));
	}
	
	public void testShouldBeValidForTagCommentRssPath() throws Exception {		
		request.setPathInfo("/comment/rss");
		assertTrue(modelBuilder.isValid(request));
	}
	
	public void testShouldBeValidForTagCommentJSONPath() throws Exception {		
		request.setPathInfo("/comment/json");
		assertTrue(modelBuilder.isValid(request));
	}
	
}
