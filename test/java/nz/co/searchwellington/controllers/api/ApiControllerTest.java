package nz.co.searchwellington.controllers.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
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

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


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
		stub(adminUser.isAdmin()).toReturn(true);
		stub(loggedInUserFilter.getLoggedInUser()).toReturn(adminUser);
	}
	
	
	public void testShouldAcceptFeedItemByUrl() throws Exception {	
		ApiController controller = new ApiController(resourceDAO, requestFilter, loggedInUserFilter, supressionService, rssNewsitemService, contentUpdateService, null, autoTaggingService, null);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
				
		request.setParameter("url", "http://test/233");
		stub(rssNewsitemService.getFeedNewsitemByUrl("http://test/233")).toReturn(acceptedNewsitem);
		controller.accept(request, response);
		
		verify(contentUpdateService).update(acceptedNewsitem);		
	}
	
}
