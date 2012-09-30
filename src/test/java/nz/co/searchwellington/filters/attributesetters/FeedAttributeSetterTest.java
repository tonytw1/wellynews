package nz.co.searchwellington.filters.attributesetters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

public class FeedAttributeSetterTest {
	
	@Mock HibernateResourceDAO resourceDAO;
	@Mock Feed feed;
	private MockHttpServletRequest request;
	private FeedAttributeSetter feedAttributeSetter;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		when(feed.getName()).thenReturn("Wellington City Council news");
		when(resourceDAO.loadFeedByUrlWords("wcc-news")).thenReturn(feed);
		feedAttributeSetter = new FeedAttributeSetter(resourceDAO);		
	}
	
	@Test
	public void shouldSetFeedAttributeForFeedPagePath() {
		request.setPathInfo("/feed/wcc-news");		
		feedAttributeSetter.setAttributes(request);		
		assertEquals(feed, request.getAttribute(FeedAttributeSetter.FEED_ATTRIBUTE));		
	}
	
	@Test
	public void shouldSetFeedAttributeForFeedEditPagePath() {
		request.setPathInfo("/feed/wcc-news/edit");
		feedAttributeSetter.setAttributes(request);
		assertEquals(feed, request.getAttribute(FeedAttributeSetter.FEED_ATTRIBUTE));		
	}
	
	@Test
	public void shouldSetFeedAttributeForFeedSavePath() {
		request.setPathInfo("/feed/wcc-news/save");
		feedAttributeSetter.setAttributes(request);
		assertEquals(feed, request.getAttribute(FeedAttributeSetter.FEED_ATTRIBUTE));		
	}
	
}
