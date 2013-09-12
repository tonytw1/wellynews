package nz.co.searchwellington.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.queues.LinkCheckerQueue;
import nz.co.searchwellington.repositories.FrontendContentUpdater;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ContentUpdateServiceTest {

	@Mock private HibernateResourceDAO resourceDAO;
	@Mock private LinkCheckerQueue linkCheckerQueue;
	
	@Mock private Newsitem exitingResource;
	@Mock private Newsitem updatedResource;
	@Mock private Newsitem newResource;
	@Mock private FrontendContentUpdater frontendContentUpdater;
	
	private ContentUpdateService service;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(exitingResource.getId()).thenReturn(1);
		when(exitingResource.getType()).thenReturn("N");
		when(exitingResource.getUrl()).thenReturn("http://test/abc");
		
		when(updatedResource.getId()).thenReturn(1);
		when(updatedResource.getType()).thenReturn("N");
		when(updatedResource.getUrl()).thenReturn("http://test/123");
				
		when(newResource.getId()).thenReturn(0);
		when(newResource.getType()).thenReturn("W");
		
		when(resourceDAO.loadResourceById(1)).thenReturn(updatedResource);
		
		service = new ContentUpdateService(resourceDAO, linkCheckerQueue, frontendContentUpdater);
	}
	
	@Test
	public void shouldSaveThroughTheHibernateDAO() throws Exception {		
		service.update(updatedResource);
		verify(resourceDAO).saveResource(updatedResource);
	}
	
	@Test
	public void shouldUpdateTheFrontendSolrIndexOnSave() throws Exception {
		service.update(updatedResource);
		verify(frontendContentUpdater).update(updatedResource);
	}
	
	/*
	@Test
	public void shouldInitHttpStatusOwnerAndQueueLinkCheckForNewSubmissions() throws Exception {		
		service.update(newResource);
		verify(newResource).setHttpStatus(0);
		verify(linkCheckerQueue).add(newResource);
	}
	*/
	
	/*
	@Test
	public void shouldInitHttpStatusAndQueueLinkCheckWhenUrlChanges() throws Exception {	
		when(resourceDAO.loadResourceById(1)).thenReturn(exitingResource);
		service.update(updatedResource);
		verify(updatedResource).setHttpStatus(0);
		verify(linkCheckerQueue).add(updatedResource);
	}
	*/
	
}
