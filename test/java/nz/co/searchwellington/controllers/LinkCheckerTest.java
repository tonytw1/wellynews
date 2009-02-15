package nz.co.searchwellington.controllers;

import junit.framework.TestCase;

public class LinkCheckerTest extends TestCase {
    
    private LinkChecker linkChecker;
    
   
    
    @Override
    protected void setUp() throws Exception {
        linkChecker = new LinkChecker();
    }
    
    
  
 
    public void testShouldGuessCommentFeedUrlForWordpressNumberedUrls() throws Exception {        
//        Newsitem wordpressNumberedPost = new NewsitemImpl(1, "Test", CommentFeedUrlTest.WORD_PRESS_NUMBERED_POST_URL, "test", null, null, null, new HashSet<DiscoveredFeed>());                       
//        final Set<String> discoveredUrls = linkChecker.getAutoDiscoveredUrlsFromResource(wordpressNumberedPost);
//        assertEquals(1, discoveredUrls.size());
//        assertEquals(CommentFeedUrlTest.WORD_PRESS_NUMBERED_POSTS_COMMENT_URL, discoveredUrls.iterator().next());
//        
        // TODO should not need resourceDAO todo this.
        //linkChecker.discoverFeeds(wordpressNumberedPost);
        //assertNotNull(wordpressNumberedPost.getCommentFeed());
    }
     
    
    
}
