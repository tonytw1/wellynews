package nz.co.searchwellington.filters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;

import org.springframework.mock.web.MockHttpServletRequest;


public class RequestFilterTest extends TestCase {
	
	private ResourceRepository resourceDAO = mock(ResourceRepository.class);	// TODO needs to use CRS
	private TagDAO tagDAO = mock(TagDAO.class);
	private GoogleSearchTermExtractor searchTermExtractor = mock(GoogleSearchTermExtractor.class);

	private RequestFilter filter;
	private Tag transportTag = mock(Tag.class);
	private Tag soccerTag = mock(Tag.class);
	private Website capitalTimesPublisher = mock(Website.class);
	private Feed feed = mock(Feed.class);
	
	
	@Override
	protected void setUp() throws Exception {
		when(tagDAO.loadTagByName("transport")).thenReturn(transportTag);
		when(tagDAO.loadTagByName("soccer")).thenReturn(soccerTag);
		when(resourceDAO.getPublisherByUrlWords("capital-times")).thenReturn(capitalTimesPublisher);
		when(resourceDAO.loadFeedByUrlWords("tranz-metro-delays")).thenReturn(feed);
		filter = new RequestFilter(resourceDAO, tagDAO, searchTermExtractor);
	}
	
	
	 
	 public void testShouldPopulateTagForAutotagUrl() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/autotag/transport");
		 filter.loadAttributesOntoRequest(request);
		 verify(tagDAO).loadTagByName("transport");		
	 }
	 
	
	public void testShouldParsePageAttribute() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("/transport");
		request.setParameter("page", "3");
		filter.loadAttributesOntoRequest(request);
		Integer page = (Integer) request.getAttribute("page");
		assertEquals(3, page.intValue());
	}
	
	
	public void testShouldPopulateFeedFromRequestParameter() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/feed/tranz-metro-delays");
		 
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadFeedByUrlWords("tranz-metro-delays");
		 assertNotNull(request.getAttribute("feedAttribute"));
		 assertEquals(feed, request.getAttribute("feedAttribute"));
		 verifyNoMoreInteractions(resourceDAO);		 
	}
		
	
	public void testShouldNotAttemptToResolveTagForReservedUrlWordComment() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/comment");		 
		 filter.loadAttributesOntoRequest(request);
		 verifyNoMoreInteractions(resourceDAO);		
	}
	
	public void testShouldNotAttemptToResolveTagForReservedUrlWordGeotagged() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/geotagged/rss");		 
		 filter.loadAttributesOntoRequest(request);
		 verifyNoMoreInteractions(resourceDAO);		
	}
		
	 public void testShouldPopulateTagForSingleTagCommentRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport/comment");
		 filter.loadAttributesOntoRequest(request);
		 verify(tagDAO).loadTagByName("transport");
		 assertEquals(transportTag, request.getAttribute("tag"));
	 }
	 
	 
	 public void testShouldPopulateTagForSingleTagCommentRssRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport/comment/rss");
		 filter.loadAttributesOntoRequest(request);
		 verify(tagDAO).loadTagByName("transport");		
	 }
	 
	 
	 public void testShouldPopulatePublisherForPublisherRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/capital-times");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).getPublisherByUrlWords("capital-times");
		 assertEquals(capitalTimesPublisher, request.getAttribute("publisher"));
	 }
	
	 
	 public void testShouldPopulateTagForSingleTagGeotagRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport/geotagged");
		 filter.loadAttributesOntoRequest(request);
		 verify(tagDAO).loadTagByName("transport");
		 assertEquals(transportTag, request.getAttribute("tag"));
	 }
	 	 
	 public void testShouldPopulateTagForSingleTagRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport");
		 filter.loadAttributesOntoRequest(request);
		 verify(tagDAO).loadTagByName("transport");
		 assertEquals(transportTag, request.getAttribute("tag"));
	 }
	    
	 public void testShouldPopulateTagForSingleTagRssRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport/rss");
		 filter.loadAttributesOntoRequest(request);
		 verify(tagDAO).loadTagByName("transport");
		 assertEquals(transportTag, request.getAttribute("tag"));
	 }
	 
	 
	 public void testShouldPopulateAttributesForPublisherTagCombinerRequest() throws Exception {
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

	 
	 public void testShouldPopulateAttributesForPublisherTagCombinerRssRequest() throws Exception {
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

	 
	 public void testShouldPopulateTagsForTagCombinerRequest() throws Exception {
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
	 
	 	 
	 public void testShouldPopulateTagsForTagCombinerJSONRequest() throws Exception {
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
	 
	 
}
