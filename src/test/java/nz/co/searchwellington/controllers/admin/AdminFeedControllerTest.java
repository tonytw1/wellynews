package nz.co.searchwellington.controllers.admin;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.feeds.FeedReader;
import nz.co.searchwellington.feeds.rss.RssNewsitemPrefetcher;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedAcceptancePolicy;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.urls.UrlBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AdminFeedControllerTest {

	private static final int FEED_ID = 1;
	
	@Mock AdminRequestFilter requestFilter;
	@Mock FeedReader feedReader;
	@Mock RssNewsitemPrefetcher rssPrefetcher;
	@Mock UrlBuilder urlBuilder;
	@Mock EditPermissionService permissionService;
	@Mock LoggedInUserFilter loggedInUserFilter;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@Mock User loggedInUser;

	@Mock Feed feed;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();		
		Mockito.when(feed.getId()).thenReturn(FEED_ID);
		Mockito.when(feed.getName()).thenReturn("A feed");
	}

	@Test
	public void manualFeedReaderRunsShouldBeAttributedToTheUserWhoKicksThemOffAndShouldAcceptAllEvenIfNoDateIsGivenOfNotCurrent() throws Exception {		
		Mockito.when(loggedInUserFilter.getLoggedInUser()).thenReturn(loggedInUser);
		Mockito.when(permissionService.canAcceptAllFrom(feed)).thenReturn(true);		
		AdminFeedController controller = new AdminFeedController(requestFilter, feedReader, rssPrefetcher, urlBuilder, permissionService, loggedInUserFilter);
		
		request.setAttribute("feedAttribute", feed);
		controller.acceptAllFrom(request, response);
		
		Mockito.verify(feedReader).processFeed(FEED_ID, loggedInUser, FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES);
	}
	
}
