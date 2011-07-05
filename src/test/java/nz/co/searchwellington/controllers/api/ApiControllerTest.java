package nz.co.searchwellington.controllers.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SupressionService;
import nz.co.searchwellington.tagging.AutoTaggingService;

public class ApiControllerTest extends TestCase {
	
	ResourceRepository	resourceDAO = mock(ResourceRepository.class);
	AdminRequestFilter	requestFilter = mock(AdminRequestFilter.class);
	LoggedInUserFilter	loggedInUserFilter = mock(LoggedInUserFilter.class);
	SupressionService supressionService = mock(SupressionService.class);
	RssfeedNewsitemService rssNewsitemService = mock(RssfeedNewsitemService.class);
	ContentUpdateService contentUpdateService = mock(ContentUpdateService.class);
	AutoTaggingService autoTaggingService = mock(AutoTaggingService.class);

	Newsitem acceptedNewsitem = mock(Newsitem.class);
	User adminUser = mock(User.class);

	protected void setUp() throws Exception {
		when(adminUser.isAdmin()).thenReturn(true);
		when(loggedInUserFilter.getLoggedInUser()).thenReturn(adminUser);
	}
	
	// TODO reenable
	public void testShouldAcceptFeedItemByUrl() throws Exception {	
		/*
		ApiController controller = new ApiController(resourceDAO, requestFilter, loggedInUserFilter, supressionService, rssNewsitemService, contentUpdateService, null, autoTaggingService, null);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("url", "http://test/233");
		when(rssNewsitemService.getFeedNewsitemByUrl("http://test/233")).thenReturn(acceptedNewsitem);
		controller.accept(request, response);
		verify(contentUpdateService).update(acceptedNewsitem);		
		 */
	}
	
}
