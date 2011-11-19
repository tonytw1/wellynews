package nz.co.searchwellington.controllers.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class AdminRequestFilterTest {
	
	private ResourceRepository resourceDAO = mock(ResourceRepository.class);
	private AdminRequestFilter filter;
	private Tag transportTag = mock(Tag.class);
	private Feed feed = mock(Feed.class);
	private Resource resource = mock(Resource.class);
	private Website publisher = mock(Website.class);
	private TagDAO tagDAO = mock(TagDAO.class);
	
	@Before
	public void setUp() throws Exception {
		when(tagDAO.loadTagByName("transport")).thenReturn(transportTag);
		when(resourceDAO.loadResourceById(123)).thenReturn(feed);
		when(resourceDAO.loadResourceById(567)).thenReturn(resource);
		filter = new AdminRequestFilter(resourceDAO, tagDAO);
	}
	
	@Test
	public void testShouldPopulateParentTagAttribute() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/tag/save");
		 request.setParameter("parent", "transport");
		 assertNotNull(filter);
		 filter.loadAttributesOntoRequest(request);		 
		 assertNotNull(request.getAttribute("parent_tag"));
	}
	
	@Test
	public void testShouldParseDateParameterIntoDateAttribute() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/save");
		 request.setParameter("date", "23 Apr 2009");
		 filter.loadAttributesOntoRequest(request);
		 assertNotNull(request.getAttribute("date"));
		 DateTime date = new DateTime((Date) request.getAttribute("date"));
		 assertEquals(new DateTime(2009, 4, 23, 0, 0, 0, 0), date);		
	}
	
	@Test
	public void testShouldPopulateResourceFromParameter() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/edit");
		 request.setParameter("resource", "567");
		 filter.loadAttributesOntoRequest(request);		 
		 assertNotNull(request.getAttribute("resource"));
		 Resource requestResource = (Resource) request.getAttribute("resource");
		 assertEquals(resource, requestResource);
	}
	
	@Test
	public void testShouldPutTagOntoEditTagPath() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/tag/transport");
		 filter.loadAttributesOntoRequest(request);
		 verify(tagDAO).loadTagByName("transport");
		 
		 Tag requestTag = (Tag) request.getAttribute("tag");
		 assertNotNull(requestTag);
	}
	
	@Test
	public void testShouldPopulateTagFromParameterAsWell() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/tag/save");
		 request.setParameter("tag", "transport");
		 filter.loadAttributesOntoRequest(request);
		 verify(tagDAO).loadTagByName("transport");
		 
		 Tag requestTag = (Tag) request.getAttribute("tag");
		 assertNotNull(requestTag);
		 assertEquals(transportTag, requestTag);
	}
		
	@Test
	public void testShouldPopulateFeedAttributeFromParameter() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/tag/save");
		 request.setParameter("feed", "123");
		 filter.loadAttributesOntoRequest(request);		 
		 assertNotNull(request.getAttribute("feedAttribute"));
	}
		
}
