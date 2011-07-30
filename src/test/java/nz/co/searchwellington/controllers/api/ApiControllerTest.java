package nz.co.searchwellington.controllers.api;

import static org.mockito.Mockito.when;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.SubmissionProcessingService;
import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
import nz.co.searchwellington.feeds.FeedItemAcceptor;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SupressionService;
import nz.co.searchwellington.tagging.AutoTaggingService;

import org.apache.struts.mock.MockHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

public class ApiControllerTest {
	
	@Mock ResourceRepository resourceDAO;
	@Mock AdminRequestFilter requestFilter;
	@Mock LoggedInUserFilter loggedInUserFilter;
	@Mock SupressionService supressionService;
	@Mock RssfeedNewsitemService rssNewsitemService;
	@Mock SubmissionProcessingService submissionProcessingService;
	@Mock ContentUpdateService contentUpdateService;
	@Mock AutoTaggingService autoTaggingService;
	@Mock FeedItemAcceptor feedItemAcceptor;
	
	@Mock Newsitem acceptedFeedNewsitem;
	@Mock User adminUser;
	private HandTaggingDAO tagVoteDao;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(adminUser.getName()).thenReturn("An admin user");
		when(adminUser.isAdmin()).thenReturn(true);
		when(loggedInUserFilter.getLoggedInUser()).thenReturn(adminUser);
	}
	
	@Test
	public void authorisedUsersCanRemotelyAcceptFeedsItemByUrl() throws Exception {
		ApiController controller = new ApiController(resourceDAO,
				requestFilter, loggedInUserFilter, supressionService,
				rssNewsitemService, contentUpdateService,
				submissionProcessingService, autoTaggingService, tagVoteDao, feedItemAcceptor);

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("url", "http://test/233");
		when(rssNewsitemService.getFeedNewsitemByUrl("http://test/233")).thenReturn(acceptedFeedNewsitem);
		controller.accept(request, response);

		Mockito.verify(autoTaggingService).autotag(acceptedFeedNewsitem);	// TODO Post accept steps should be shared with the normal accept method!
		Mockito.verify(contentUpdateService).update(acceptedFeedNewsitem);		
	}
	
}
