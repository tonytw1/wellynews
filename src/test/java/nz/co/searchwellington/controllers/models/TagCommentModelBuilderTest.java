package nz.co.searchwellington.controllers.models;

import static org.mockito.Mockito.mock;

import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Tag;

import org.springframework.mock.web.MockHttpServletRequest;

import com.google.common.collect.Lists;

public class TagCommentModelBuilderTest extends TestCase {

	private Tag tag = mock(Tag.class);
	private List<Tag> tags;
	private MockHttpServletRequest request;
	
	@Override
	protected void setUp() throws Exception {
		request = new MockHttpServletRequest();
		tags = Lists.newArrayList();	
		tags.add(tag);	
		request.setAttribute("tags", tags);
	}
		
	public void testShouldBeValidForTagCommentPath() throws Exception {
		ModelBuilder modelBuilder = new TagCommentModelBuilder(null, null, null);		
		request.setPathInfo("/transport/comment");
		assertTrue(modelBuilder.isValid(request));
	}
		
	public void testShouldBeValidForTagCommentRssPath() throws Exception {
		ModelBuilder modelBuilder = new TagCommentModelBuilder(null, null, null);		
		request.setPathInfo("/transport/comment/rss");
		assertTrue(modelBuilder.isValid(request));
	}
	
	
	public void testShouldBeValidForTagCommentJSONPath() throws Exception {
		ModelBuilder modelBuilder = new TagCommentModelBuilder(null, null, null);		
		request.setPathInfo("/transport/comment/json");
		assertTrue(modelBuilder.isValid(request));
	}
		
}
