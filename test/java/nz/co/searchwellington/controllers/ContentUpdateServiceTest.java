package nz.co.searchwellington.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;
import nz.co.searchwellington.mail.Notifier;
import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionDAO;

import org.springframework.mock.web.MockHttpServletRequest;

public class ContentUpdateServiceTest extends TestCase {

	private ContentUpdateService service;
	private ResourceRepository resourceDAO = mock(ResourceRepository.class);
	private SuggestionDAO suggestionsDAO = mock(SuggestionDAO.class);
	private LinkCheckerQueue linkCheckerQueue = mock(LinkCheckerQueue.class);
	private Notifier notifier = mock(Notifier.class);
	private MockHttpServletRequest request = new MockHttpServletRequest();
	
	
	private Newsitem updatedResource = mock(Newsitem.class);
	private User loggedInUser = mock(User.class);
	
	
	@Override
	protected void setUp() throws Exception {
		service = new ContentUpdateService(resourceDAO, suggestionsDAO, linkCheckerQueue, notifier);
		stub(updatedResource.getId()).toReturn(1);
		stub(updatedResource.getType()).toReturn("N");
		stub(updatedResource.getUrl()).toReturn("http://test/123");
	}
	
	
	public void testShouldSaveThroughTheDAO() throws Exception {		
		service.update(updatedResource, loggedInUser, request, false, false);
		verify(resourceDAO).saveResource(updatedResource);
	}
		
	
	public void testShouldRemoveSuggestionsForNewsitems() throws Exception {		
		service.update(updatedResource, loggedInUser, request, false, false);
		verify(suggestionsDAO).removeSuggestion("http://test/123");
	}
	
	
	public void testShouldInitHttpStatusAndQueueLinkCheckForNewSubmissions() throws Exception {		
		service.update(updatedResource, loggedInUser, request, true, false);
		verify(updatedResource).setHttpStatus(0);
		verify(linkCheckerQueue).add(1);
	}
	
	
	public void testShouldInitHttpStatusAndQueueLinkCheckWhenUrlChanges() throws Exception {		
		service.update(updatedResource, loggedInUser, request, false, true);
		verify(updatedResource).setHttpStatus(0);
		verify(linkCheckerQueue).add(1);
	}

	
	public void testShouldSendNotificationOfNewPublicSubmissions() throws Exception {		
		service.update(updatedResource, null, request, true, false);
		verify(notifier).sendSubmissionNotification("New submission", updatedResource);
	}
	
}
