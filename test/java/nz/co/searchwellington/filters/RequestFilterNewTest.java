package nz.co.searchwellington.filters;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.springframework.mock.web.MockHttpServletRequest;


public class RequestFilterNewTest extends TestCase {
	
	private ResourceRepository resourceDAO = mock(ResourceRepository.class);
	private RequestFilter filter;
	private Tag transportTag = mock(Tag.class);
	private Tag soccerTag = mock(Tag.class);
	private Website capitalTimesPublisher = mock(Website.class);
	private Feed feed = mock(Feed.class);
	
	@Override
	protected void setUp() throws Exception {		 
		stub(resourceDAO.loadTagByName("transport")).toReturn(transportTag);
		stub(resourceDAO.loadTagByName("soccer")).toReturn(soccerTag);
		stub(resourceDAO.getPublisherByUrlWords("capital-times")).toReturn(capitalTimesPublisher);
		stub(resourceDAO.loadResourceById(123)).toReturn(feed);
		filter = new RequestFilter(resourceDAO);
	 }
	
	
	public void testShouldIgnoreTagAndPublisherLookupsForArchivePaths() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/archive/2009/feb");
		 filter.loadAttributesOntoRequest(request);
		 verifyNoMoreInteractions(resourceDAO);
	}
		
	public void testShouldPopulateFeedFromRequestParameter() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/viewfeed");
		 request.setParameter("feed", "123");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadResourceById(123);
		 assertNotNull(request.getAttribute("feedAttribute"));
		 assertEquals(feed, request.getAttribute("feedAttribute"));
		 verifyNoMoreInteractions(resourceDAO);		 
	}
		
		
	 public void testShouldPopulateTagForSingleTagCommentRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport/comment");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadTagByName("transport");
		 assertEquals(transportTag, request.getAttribute("tag"));
	 }
	 
	 
	 public void testShouldPopulateTagForSingleTagCommentRssRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport/comment/rss");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadTagByName("transport");		
	 }
	 
	 
	 public void testShouldPopulatePublisherForPublisherRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/capital-times");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).getPublisherByUrlWords("capital-times");
		 assertEquals(capitalTimesPublisher, request.getAttribute("publisher"));
	 }
	 
	 public void testShouldPopulatePublisherForPublisherWatchlistRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/capital-times/watchlist");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).getPublisherByUrlWords("capital-times");
		 assertEquals(capitalTimesPublisher, request.getAttribute("publisher"));
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
	 
	 
	 public void testShouldPopulateAttributesForPublisherTagCombinerRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/capital-times+soccer");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).getPublisherByUrlWords("capital-times");		 
		 verify(resourceDAO).loadTagByName("soccer");
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
		 verify(resourceDAO).loadTagByName("soccer");
		 Website publisher = (Website) request.getAttribute("publisher");
		 Tag tag = (Tag) request.getAttribute("tag");
		 assertEquals(capitalTimesPublisher, publisher);
		 assertEquals(soccerTag, tag);
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
	 
	 
	 
	 public void testShouldPopulateTagsForTagCombinerJSONRequest() throws Exception {
		 MockHttpServletRequest request = new MockHttpServletRequest();
		 request.setPathInfo("/transport+soccer/json");
		 filter.loadAttributesOntoRequest(request);
		 verify(resourceDAO).loadTagByName("transport");
		 verify(resourceDAO).loadTagByName("soccer");		 
		 List<Tag> tags = (List<Tag>) request.getAttribute("tags");
		 assertEquals(2, tags.size());		 
		 assertEquals(transportTag, tags.get(0));
		 assertEquals(soccerTag, tags.get(1));
	 }
	 
	 
}
