package nz.co.searchwellington.controllers.admin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

import java.util.Date;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.joda.time.DateTime;
import org.springframework.mock.web.MockHttpServletRequest;

public class AdminRequestFilterTest extends TestCase {
	
	private ResourceRepository resourceDAO = mock(ResourceRepository.class);
	private AdminRequestFilter filter;
	private Tag transportTag = mock(Tag.class);
	private Feed feed = mock(Feed.class);
	private Resource resource = mock(Resource.class);
	
	@Override
	protected void setUp() throws Exception {		 
		stub(resourceDAO.loadTagByName("transport")).toReturn(transportTag);
		stub(resourceDAO.loadResourceById(123)).toReturn(feed);
		stub(resourceDAO.loadResourceById(567)).toReturn(resource);
		filter = new AdminRequestFilter(resourceDAO);
	}

	
	public void testShouldPopulateParentTagAttribute() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/tag/save");
		 request.setParameter("parent", "transport");
		 filter.loadAttributesOntoRequest(request);		 
		 assertNotNull(request.getAttribute("parent_tag"));
	}
	
	public void testShouldParseDateParameterIntoDateAttribute() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/save");
		 request.setParameter("date", "23 Apr 2009");
		 filter.loadAttributesOntoRequest(request);
		 assertNotNull(request.getAttribute("date"));
		 DateTime date = new DateTime((Date) request.getAttribute("date"));
		 assertEquals(new DateTime(2009, 4, 23, 0, 0, 0, 0), date);		
	}
	
	
	
	public void testShouldPopulateResourceFromParameter() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/edit");
		 request.setParameter("resource", "567");
		 filter.loadAttributesOntoRequest(request);		 
		 assertNotNull(request.getAttribute("resource"));
		 Resource requestResource = (Resource) request.getAttribute("resource");
		 assertEquals(resource, requestResource);
	}
	
	
	public void testShouldPutTagOntoEditTagPath() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/tag/transport");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadTagByName("transport");
		 
		 Tag requestTag = (Tag) request.getAttribute("tag");
		 assertNotNull(requestTag);
	}
	
		
	public void testShouldPopulateTagFromParameterAsWell() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/tag/save");
		 request.setParameter("tag", "transport");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadTagByName("transport");
		 
		 Tag requestTag = (Tag) request.getAttribute("tag");
		 assertNotNull(requestTag);
		 assertEquals(transportTag, requestTag);
	}
		
	
	public void testShouldPopulateFeedAttributeFromParameter() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/tag/save");
		 request.setParameter("feed", "123");
		 filter.loadAttributesOntoRequest(request);		 
		 assertNotNull(request.getAttribute("feedAttribute"));
	}
		
}
