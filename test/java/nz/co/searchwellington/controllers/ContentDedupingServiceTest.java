package nz.co.searchwellington.controllers;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.WebsiteImpl;

public class ContentDedupingServiceTest extends TestCase {
    
    Newsitem firstCommentedNewsitem = new NewsitemImpl();      
    Newsitem secondCommentedNewsitem = new NewsitemImpl();
    
    
    public void testShouldDedupeCommentedNewsitemsFromIndexPageNewsitems() throws Exception {
      List<Newsitem> commentedNewsitemOnPage = new ArrayList<Newsitem>();
      List<Newsitem> newsitemsOnPage = new ArrayList<Newsitem>();
      
      Newsitem newsitem = new NewsitemImpl();
      
      commentedNewsitemOnPage.add(firstCommentedNewsitem);
      commentedNewsitemOnPage.add(secondCommentedNewsitem);
      
      newsitemsOnPage.add(firstCommentedNewsitem);
      newsitemsOnPage.add(newsitem);
      
      ContentDedupingService dedupingService = new ContentDedupingService();
      dedupingService.dedupeIndexPageNewsitems(newsitemsOnPage, commentedNewsitemOnPage);
      
      assertFalse(newsitemsOnPage.contains(firstCommentedNewsitem));                
    }
    
    
    
    
    public void testShouldNotDedupeCommentedNewsitemsFromSingleColumnTagPage() throws Exception {
        List<Resource> commentedNewsitemOnPage = new ArrayList<Resource>();
        List<Resource> newsitemsOnPage = new ArrayList<Resource>();
        List<Website> websitesOnPage = new ArrayList<Website>();
        
        Newsitem firstCommentedNewsitem = new NewsitemImpl();      
        Newsitem secondCommentedNewsitem = new NewsitemImpl();
        Newsitem newsitem = new NewsitemImpl();
        
        commentedNewsitemOnPage.add(firstCommentedNewsitem);
        commentedNewsitemOnPage.add(secondCommentedNewsitem);
        
        newsitemsOnPage.add(firstCommentedNewsitem);
        newsitemsOnPage.add(newsitem);
               
        ContentDedupingService dedupingService = new ContentDedupingService();
        dedupingService.dedupeTagPageNewsitems(newsitemsOnPage, commentedNewsitemOnPage, websitesOnPage);
        
        assertTrue(newsitemsOnPage.contains(firstCommentedNewsitem));                
      }
    
    
    
    
    public void testShouldDedupeCommentedNewsitemsFromTwoColumnTagPage() throws Exception {
        List<Resource> commentedNewsitemOnPage = new ArrayList<Resource>();
        List<Resource> newsitemsOnPage = new ArrayList<Resource>();
        List<Website> websitesOnPage = new ArrayList<Website>();
        
        Newsitem firstCommentedNewsitem = new NewsitemImpl();      
        Newsitem secondCommentedNewsitem = new NewsitemImpl();
        Newsitem newsitem = new NewsitemImpl();
        
        commentedNewsitemOnPage.add(firstCommentedNewsitem);
        commentedNewsitemOnPage.add(secondCommentedNewsitem);
        
        newsitemsOnPage.add(firstCommentedNewsitem);
        newsitemsOnPage.add(newsitem);
        
        websitesOnPage.add(new WebsiteImpl());
               
        ContentDedupingService dedupingService = new ContentDedupingService();
        dedupingService.dedupeTagPageNewsitems(newsitemsOnPage, commentedNewsitemOnPage, websitesOnPage);
        
        assertFalse(newsitemsOnPage.contains(firstCommentedNewsitem));                
      }
    

}
