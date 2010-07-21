package nz.co.searchwellington.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;
import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SuggestionDAO;
import nz.co.searchwellington.repositories.solr.SolrUpdateQueue;

public class ContentUpdateServiceTest extends TestCase {

	private ContentUpdateService service;
	private ResourceRepository resourceDAO = mock(ResourceRepository.class);
	private SuggestionDAO suggestionsDAO = mock(SuggestionDAO.class);
	private LinkCheckerQueue linkCheckerQueue = mock(LinkCheckerQueue.class);
	private SolrUpdateQueue solrUpdateQueue = mock(SolrUpdateQueue.class);
		
	private Newsitem exitingResource = mock(Newsitem.class);
	private Newsitem updatedResource = mock(Newsitem.class);
	private Newsitem newResource = mock(Newsitem.class);
	
	
	@Override
	protected void setUp() throws Exception {
		service = new ContentUpdateService(resourceDAO, suggestionsDAO, linkCheckerQueue, solrUpdateQueue);
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

    
    public void testShouldUpdateTheSolrIndexOnSave() throws Exception {		
		service.update(updatedResource);
		verify(solrUpdateQueue).add(updatedResource);
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
