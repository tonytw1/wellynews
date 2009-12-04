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
	
	private Newsitem exitingResource = mock(Newsitem.class);
	private Newsitem updatedResource = mock(Newsitem.class);
	private Newsitem newResource = mock(Newsitem.class);
	private User loggedInUser = mock(User.class);
	
	
	@Override
	protected void setUp() throws Exception {
		service = new ContentUpdateService(resourceDAO, suggestionsDAO, linkCheckerQueue, notifier);
		stub(exitingResource.getId()).toReturn(1);
		stub(exitingResource.getType()).toReturn("N");
		stub(exitingResource.getUrl()).toReturn("http://test/abc");
		
		stub(updatedResource.getId()).toReturn(1);
		stub(updatedResource.getType()).toReturn("N");
		stub(updatedResource.getUrl()).toReturn("http://test/123");
				
		stub(newResource.getId()).toReturn(0);
		stub(newResource.getType()).toReturn("W");
		
		stub(resourceDAO.loadResourceById(1)).toReturn(updatedResource);
	}
	
	
	public void testShouldSaveThroughTheDAO() throws Exception {		
		service.update(updatedResource, loggedInUser, request);
		verify(resourceDAO).saveResource(updatedResource);
	}
		
	
	public void testShouldRemoveSuggestionsForNewsitems() throws Exception {		
		service.update(updatedResource, loggedInUser, request);
		verify(suggestionsDAO).removeSuggestion("http://test/123");
	}
	
	
	public void testShouldInitHttpStatusOwnerAndQueueLinkCheckForNewSubmissions() throws Exception {		
		service.update(newResource, loggedInUser, request);
		verify(newResource).setHttpStatus(0);
		verify(linkCheckerQueue).add(newResource);
		verify(newResource).setOwner(loggedInUser);
	}
	
	public void testShouldInitHttpStatusAndQueueLinkCheckWhenUrlChanges() throws Exception {	
		stub(resourceDAO.loadResourceById(1)).toReturn(exitingResource);
		service.update(updatedResource, loggedInUser, request);
		verify(updatedResource).setHttpStatus(0);
		verify(linkCheckerQueue).add(updatedResource);
	}
	
	public void testShouldSendNotificationOfNewPublicSubmissions() throws Exception {		
		service.update(newResource, null, request);		
		verify(notifier).sendSubmissionNotification("New submission", newResource);
	}
	
}
