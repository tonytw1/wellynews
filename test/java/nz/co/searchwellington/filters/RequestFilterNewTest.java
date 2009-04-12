package nz.co.searchwellington.filters;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.springframework.mock.web.MockHttpServletRequest;


public class RequestFilterNewTest extends TestCase {
	
	private ResourceRepository resourceDAO = mock(ResourceRepository.class);
	private RequestFilter filter;
	private Tag transportTag = mock(Tag.class);
	private Tag soccerTag = mock(Tag.class);
	
	 @Override
	 protected void setUp() throws Exception {		 
		 stub(resourceDAO.loadTagByName("transport")).toReturn(transportTag);
		 stub(resourceDAO.loadTagByName("soccer")).toReturn(soccerTag);
		 filter = new RequestFilter(resourceDAO);		 
	 }
		
	
	 public void testShouldPopulateTagForSingleTagCommentRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport/comment");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadTagByName("transport");
		 assertEquals(transportTag, request.getAttribute("tag"));
	 }
	 
	 
	 public void testShouldPopulateTagForSingleTagGeotagRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport/geotagged");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadTagByName("transport");
		 assertEquals(transportTag, request.getAttribute("tag"));
	 }
	 	 
	 public void testShouldPopulateTagForSingleTagRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadTagByName("transport");
		 assertEquals(transportTag, request.getAttribute("tag"));
	 }
	    
	 public void testShouldPopulateTagForSingleTagRssRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport/rss");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadTagByName("transport");
		 assertEquals(transportTag, request.getAttribute("tag"));
	 }
	 
	
	 
	 public void testShouldPopulateTagsForTagCombinerRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport+soccer");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadTagByName("transport");
		 verify(resourceDAO).loadTagByName("soccer");		 
		 List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		 assertEquals(2, tags.size());		 
		 assertEquals(transportTag, tags.get(0));
		 assertEquals(soccerTag, tags.get(1));
	 }
	 
	 
}
