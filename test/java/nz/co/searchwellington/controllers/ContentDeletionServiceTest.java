package nz.co.searchwellington.controllers;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.modification.ContentDeletionService;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SnapshotDAO;
import nz.co.searchwellington.repositories.SupressionService;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.repositories.solr.SolrQueryService;

import org.mockito.MockitoAnnotations;
import org.mockito.Mock;

public class ContentDeletionServiceTest extends TestCase {
		
	@Mock SupressionService supressionService;	
	@Mock ResourceRepository resourceDAO;
	@Mock RssfeedNewsitemService rssfeedNewsitemService;
	@Mock SnapshotDAO SnapshotDAO;
	@Mock SolrQueryService solrQueryService;
	@Mock HandTaggingDAO handTaggingDAO;
	@Mock TagDAO tagDAO;
	
	@Mock Newsitem resource;
	@Mock Feed feed;
	@Mock Tag tag;
	
	
	ContentDeletionService service;
	
	
	@Override
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		resource = new NewsitemImpl();
		resource.setId(123);
		resource.setUrl("http://blah/test");
		service = new ContentDeletionService(supressionService, rssfeedNewsitemService, resourceDAO, SnapshotDAO, solrQueryService, handTaggingDAO, tagDAO);
	}
	
	
	public void testShouldDeleteFromSolrIndex() throws Exception {
		service.performDelete(resource);
		verify(solrQueryService).deleteResourceFromIndex(resource.getId());
	}
	
	public void testShouldSuppressFeedItemsOnDelete() throws Exception {	
		when(rssfeedNewsitemService.isUrlInAcceptedFeeds(resource.getUrl())).thenReturn(true);		
		service.performDelete(resource);
		verify(supressionService).suppressUrl(resource.getUrl());
	}
	
	public void testShouldRemoveRelatedFeedFromTagsOnDelete() throws Exception {
		when(feed.getType()).thenReturn("F");
		when(tag.getRelatedFeed()).thenReturn(feed);		
		List<Tag> allTags = new ArrayList<Tag>();
		allTags.add(tag);
		when(tagDAO.getAllTags()).thenReturn(allTags);
		
		service.performDelete(feed);
		verify(tag).setRelatedFeed(null);
	}

}
