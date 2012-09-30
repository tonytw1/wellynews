package nz.co.searchwellington.filters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.TagDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

public class RequestFilterTest {
	
	@Mock private HibernateResourceDAO resourceDAO;
	@Mock private TagDAO tagDAO;
	
	@Mock private Tag transportTag;
	@Mock private Tag soccerTag;
	@Mock private Website capitalTimesPublisher;
	@Mock private Feed feed;
	
	private RequestAttributeFilter[] filters = {};
	private RequestFilter filter;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(tagDAO.loadTagByName("transport")).thenReturn(transportTag);
		when(tagDAO.loadTagByName("soccer")).thenReturn(soccerTag);
		when(resourceDAO.getPublisherByUrlWords("capital-times")).thenReturn(capitalTimesPublisher);
		when(resourceDAO.loadFeedByUrlWords("tranz-metro-delays")).thenReturn(feed);
		filter = new RequestFilter(resourceDAO, tagDAO, filters);
	}
	
	@Test
	public void shouldPopulateTagForAutotagUrl() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/transport/autotag");
		
		filter.loadAttributesOntoRequest(request);
		
		verify(tagDAO).loadTagByName("transport");		
	}
	
	public void shouldParsePageAttribute() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/transport");
		request.setParameter("page", "3");
		
		filter.loadAttributesOntoRequest(request);
		
		Integer page = (Integer) request.getAttribute("page");
		assertEquals(3, page.intValue());
	}
	
	@Test
	public void shouldNotAttemptToResolveTagForReservedUrlWordComment() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/comment");		 
		filter.loadAttributesOntoRequest(request);
		
		verifyNoMoreInteractions(resourceDAO);		
	}
	
	@Test
	public void shouldNotAttemptToResolveTagForReservedUrlWordGeotagged() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/geotagged/rss");
		
		filter.loadAttributesOntoRequest(request);
		
		verifyNoMoreInteractions(resourceDAO);		
	}
	
	@Test
	public void shouldPopulateTagForSingleTagCommentRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/transport/comment");
		
		filter.loadAttributesOntoRequest(request);
		
		verify(tagDAO).loadTagByName("transport");		
		assertEquals(transportTag, request.getAttribute("tag"));
	}
	 
	@Test 
	public void shouldPopulateTagForSingleTagCommentRssRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/transport/comment/rss");
		
		filter.loadAttributesOntoRequest(request);
		
		verify(tagDAO).loadTagByName("transport");		
	}
	 
	@Test
	public void shouldPopulatePublisherForPublisherRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/capital-times");
		
		filter.loadAttributesOntoRequest(request);
		
		verify(resourceDAO).getPublisherByUrlWords("capital-times");		
		assertEquals(capitalTimesPublisher, request.getAttribute("publisher"));
	}
	
	@Test
	public void shouldPopulateTagForSingleTagGeotagRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/transport/geotagged");

		filter.loadAttributesOntoRequest(request);
		
		verify(tagDAO).loadTagByName("transport");		
		assertEquals(transportTag, request.getAttribute("tag"));
	}
	
	@Test
	public void shouldPopulateTagForSingleTagRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/transport");
		
		filter.loadAttributesOntoRequest(request);
		
		verify(tagDAO).loadTagByName("transport");		
		assertEquals(transportTag, request.getAttribute("tag"));
	}
	
	@Test
	public void shouldPopulateTagForSingleTagRssRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/transport/rss");
		
		filter.loadAttributesOntoRequest(request);
		
		verify(tagDAO).loadTagByName("transport");
		assertEquals(transportTag, request.getAttribute("tag"));
	}
	 
	@Test 
	public void shouldPopulateAttributesForPublisherTagCombinerRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/capital-times+soccer");
		
		filter.loadAttributesOntoRequest(request);
		
		verify(resourceDAO).getPublisherByUrlWords("capital-times");		 
		verify(tagDAO).loadTagByName("soccer");		
		Website publisher = (Website) request.getAttribute("publisher");
		Tag tag = (Tag) request.getAttribute("tag");
		assertEquals(capitalTimesPublisher, publisher);
		assertEquals(soccerTag, tag);
	}

	@Test 
	public void shouldPopulateAttributesForPublisherTagCombinerRssRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/capital-times+soccer/rss");
		
		filter.loadAttributesOntoRequest(request);
		
		verify(resourceDAO).getPublisherByUrlWords("capital-times");		 
		verify(tagDAO).loadTagByName("soccer");
		Website publisher = (Website) request.getAttribute("publisher");
		Tag tag = (Tag) request.getAttribute("tag");
		assertEquals(capitalTimesPublisher, publisher);
		assertEquals(soccerTag, tag);
	}
	
	@Test 
	@SuppressWarnings("unchecked")
	public void shouldPopulateTagsForTagCombinerRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/transport+soccer");
		
		filter.loadAttributesOntoRequest(request);

		verify(tagDAO).loadTagByName("transport");
		verify(tagDAO).loadTagByName("soccer");		 
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		assertEquals(2, tags.size());		 
		assertEquals(transportTag, tags.get(0));
		assertEquals(soccerTag, tags.get(1));
	}
	 
	@Test 	 
	@SuppressWarnings("unchecked")
	public void shouldPopulateTagsForTagCombinerJSONRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/transport+soccer/json");
		
		filter.loadAttributesOntoRequest(request);
		
		verify(tagDAO).loadTagByName("transport");
		verify(tagDAO).loadTagByName("soccer");		 
		List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		assertEquals(2, tags.size());		 
		assertEquals(transportTag, tags.get(0));
		assertEquals(soccerTag, tags.get(1));
	}
	
	// TODO implement
	public void testShouldPopulateWebsiteResourceByUrlStub() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/edit/edit");
		 request.setParameter("resource", "a-publisher");
		 when(resourceDAO.getPublisherByUrlWords("a-publisher")).thenReturn(capitalTimesPublisher);
		 
		 filter.loadAttributesOntoRequest(request);		 
		 
		 assertEquals(capitalTimesPublisher, request.getAttribute("resource"));
	}
	
}
