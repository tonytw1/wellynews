package nz.co.searchwellington.repositories;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.controllers.RelatedTagsService;
import nz.co.searchwellington.controllers.ShowBrokenDecisionService;
import nz.co.searchwellington.feeds.DiscoveredFeedRepository;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.solr.KeywordSearchService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ContentRetrievalServiceTest {

	@Mock private ResourceRepository resourceDAO;
	@Mock private KeywordSearchService keywordSearchService;
	@Mock private ShowBrokenDecisionService showBrokenDecisionService;
	@Mock private TagDAO tagDAO;
	@Mock private RelatedTagsService relatedTagsService;
	@Mock private DiscoveredFeedRepository discoveredFeedsDAO;
	@Mock private SolrBackedResourceDAO solrBackedResourceDAO;
	
	@Mock private User loggedInUser;
	@Mock private List<Resource> contentOwnedByUser;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void contentOwnedByUserShouldComeFromTheDatabaseToEnsureImmediateUpdatesAreVisibleToTheUser() throws Exception {
		when(resourceDAO.getOwnedBy(loggedInUser, 10)).thenReturn(contentOwnedByUser);		
		ContentRetrievalService contentRetrievalService = new ContentRetrievalService(resourceDAO, keywordSearchService, showBrokenDecisionService, tagDAO, relatedTagsService, discoveredFeedsDAO, solrBackedResourceDAO);
		
		List<Resource> result = contentRetrievalService.getOwnedBy(loggedInUser, 10);
		
		assertEquals(result, contentOwnedByUser);		
	}
	
}
