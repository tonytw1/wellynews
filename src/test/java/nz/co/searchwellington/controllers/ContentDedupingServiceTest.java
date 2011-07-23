package nz.co.searchwellington.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.views.ContentDedupingService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ContentDedupingServiceTest {
    
	@Mock Newsitem newsitem;
    @Mock Newsitem firstCommentedNewsitem;    
    @Mock Newsitem secondCommentedNewsitem;
    
    ContentDedupingService dedupingService;
    
    @Before
    public void setup() {
		MockitoAnnotations.initMocks(this);
		dedupingService = new ContentDedupingService();
	}
        
    @Test
    public void testShouldDedupeCommentedNewsitemsFromIndexPageNewsitems() throws Exception {            
      List<Resource> newsitemsOnPage = new ArrayList<Resource>();
      newsitemsOnPage.add(firstCommentedNewsitem);
      newsitemsOnPage.add(newsitem);
      
      List<Resource> commentedNewsitemOnPage = new ArrayList<Resource>();
      commentedNewsitemOnPage.add(firstCommentedNewsitem);
      commentedNewsitemOnPage.add(secondCommentedNewsitem);
      
      List<Resource> dedupeNewsitems = dedupingService.dedupeNewsitems(newsitemsOnPage, commentedNewsitemOnPage);      
      assertFalse(dedupeNewsitems.contains(firstCommentedNewsitem));
      assertTrue(dedupeNewsitems.contains(newsitem));
    }
    
}
