package nz.co.searchwellington.commentfeeds.detectors;

import junit.framework.TestCase;

public class BlogspotCommentFeedDetectorTest extends TestCase {

    final static String BLOGSPOT_COMMENT_URL = "http://wellurban.blogspot.com/feeds/113750684886641660/comments/default";
    final static String BLOGSPOT_ATOM_URL = "http://wellurban.blogspot.com/atom.xml";        
    
    CommentFeedDetector detector = new BlogspotCommentFeedDetector();
    
    
    public void testShouldDetectBlogspotCommentFeedUrl() {
        assertTrue(detector.isValid(BLOGSPOT_COMMENT_URL));
    }
    
    
    public void testShouldIgnoreBlogspotAtomFeed() {
        assertFalse(detector.isValid(BLOGSPOT_ATOM_URL));
    }

}
