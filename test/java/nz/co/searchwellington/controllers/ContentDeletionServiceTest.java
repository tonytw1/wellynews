package nz.co.searchwellington.controllers;

import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.modification.ContentDeletionService;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SnapshotDAO;
import nz.co.searchwellington.repositories.SupressionService;
import nz.co.searchwellington.repositories.solr.SolrQueryService;

import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;

public class ContentDeletionServiceTest extends TestCase {
		
	@Mock SupressionService supressionService;	
	@Mock ResourceRepository resourceDAO;
	@Mock RssfeedNewsitemService rssfeedNewsitemService;
	@Mock SnapshotDAO SnapshotDAO;
	@Mock SolrQueryService solrQueryService;
	
	@Mock Newsitem resource;
	@Mock Feed feed;
	
	
	ContentDeletionService service;
	
	
	@Override
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		resource = new NewsitemImpl();
		resource.setId(123);
		resource.setUrl("http://blah/test");
		service = new ContentDeletionService(supressionService, rssfeedNewsitemService, resourceDAO, SnapshotDAO, solrQueryService);
	}
	
	public void testShouldDeleteFromSolrIndex() throws Exception {
		service.performDelete(resource);
		verify(solrQueryService).deleteResourceFromIndex(resource.getId());
	}
	
	public void testShouldSuppressFeedItemsOnDelete() throws Exception {	
		stub(rssfeedNewsitemService.isUrlInAcceptedFeeds(resource.getUrl())).toReturn(true);		
		service.performDelete(resource);
		verify(supressionService).suppressUrl(resource.getUrl());
	}
	
	public void testShouldRemoveRelatedFeedFromTagsOnDelete() throws Exception {
		fail();
	}

}
