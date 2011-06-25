package nz.co.searchwellington.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;
import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.FrontendContentUpdater;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionDAO;

public class ContentUpdateServiceTest extends TestCase {

	private ContentUpdateService service;
	private ResourceRepository resourceDAO = mock(ResourceRepository.class);
	private SuggestionDAO suggestionsDAO = mock(SuggestionDAO.class);
	private LinkCheckerQueue linkCheckerQueue = mock(LinkCheckerQueue.class);
	
	private Newsitem exitingResource = mock(Newsitem.class);
	private Newsitem updatedResource = mock(Newsitem.class);
	private Newsitem newResource = mock(Newsitem.class);
	private FrontendContentUpdater frontendContentUpdater = mock(FrontendContentUpdater.class);
		
	@Override
	protected void setUp() throws Exception {
		service = new ContentUpdateService(resourceDAO, suggestionsDAO, linkCheckerQueue, frontendContentUpdater);
		when(exitingResource.getId()).thenReturn(1);
		when(exitingResource.getType()).thenReturn("N");
		when(exitingResource.getUrl()).thenReturn("http://test/abc");
		
		when(updatedResource.getId()).thenReturn(1);
		when(updatedResource.getType()).thenReturn("N");
		when(updatedResource.getUrl()).thenReturn("http://test/123");
				
		when(newResource.getId()).thenReturn(0);
		when(newResource.getType()).thenReturn("W");
		
		when(resourceDAO.loadResourceById(1)).thenReturn(updatedResource);
	}
	
	public void testShouldSaveThroughTheHibernateDAO() throws Exception {		
		service.update(updatedResource);
		verify(resourceDAO).saveResource(updatedResource);
	}
	
	public void testShouldUpdateTheFrontendSolrIndexOnSave() throws Exception {
		service.update(updatedResource);
		verify(frontendContentUpdater).update(updatedResource);
	}
	
	public void testShouldRemoveSuggestionsForNewsitems() throws Exception {		
		service.update(updatedResource);
		verify(suggestionsDAO).removeSuggestion("http://test/123");
	}
		
	public void testShouldInitHttpStatusOwnerAndQueueLinkCheckForNewSubmissions() throws Exception {		
		service.update(newResource);
		verify(newResource).setHttpStatus(0);
		verify(linkCheckerQueue).add(newResource);
	}
	
	public void testShouldInitHttpStatusAndQueueLinkCheckWhenUrlChanges() throws Exception {	
		when(resourceDAO.loadResourceById(1)).thenReturn(exitingResource);
		service.update(updatedResource);
		verify(updatedResource).setHttpStatus(0);
		verify(linkCheckerQueue).add(updatedResource);
	}
	
}
