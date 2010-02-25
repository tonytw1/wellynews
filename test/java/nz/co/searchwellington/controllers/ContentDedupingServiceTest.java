package nz.co.searchwellington.controllers;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.views.ContentDedupingService;

public class ContentDedupingServiceTest extends TestCase {
    
    Newsitem firstCommentedNewsitem = new NewsitemImpl();      
    Newsitem secondCommentedNewsitem = new NewsitemImpl();
    
    
    public void testShouldDedupeCommentedNewsitemsFromIndexPageNewsitems() throws Exception {
      List<Resource> commentedNewsitemOnPage = new ArrayList<Resource>();
      List<Resource> newsitemsOnPage = new ArrayList<Resource>();
      
      Newsitem newsitem = new NewsitemImpl();
      
      commentedNewsitemOnPage.add(firstCommentedNewsitem);
      commentedNewsitemOnPage.add(secondCommentedNewsitem);
      
      newsitemsOnPage.add(firstCommentedNewsitem);
      newsitemsOnPage.add(newsitem);
      
      ContentDedupingService dedupingService = new ContentDedupingService();
      dedupingService.dedupeNewsitems(newsitemsOnPage, commentedNewsitemOnPage);
      
      assertFalse(newsitemsOnPage.contains(firstCommentedNewsitem));                
    }
    
}
