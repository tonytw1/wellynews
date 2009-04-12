package nz.co.searchwellington.controllers.models;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import static org.mockito.Mockito.mock;

import nz.co.searchwellington.model.Tag;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;


public class TagCommentModelBuilderTest extends TestCase {

	private Tag tag = mock(Tag.class);
	private List<Tag> tags;
	private MockHttpServletRequest request;
	
	@Override
	protected void setUp() throws Exception {
		request = new MockHttpServletRequest();
		tags = new ArrayList<Tag>();		
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
	
	private void setRss(ModelAndView mv, String title, String url) {
		mv.addObject("rss_title", title);
		mv.addObject("rss_url", url);
	}  
	
}
