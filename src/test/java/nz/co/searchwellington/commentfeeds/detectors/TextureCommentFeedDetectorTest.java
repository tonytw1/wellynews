package nz.co.searchwellington.commentfeeds.detectors;

import junit.framework.TestCase;

public class TextureCommentFeedDetectorTest extends TestCase {

    final static String TEXTURE_COMMENT_URL = "http://texture.co.nz/blogs/features/commentrss.aspx?PostID=6696";
    
    CommentFeedDetector detector = new TextureCommentFeedDetector();
    
    
    public void testShouldDetectBlogspotCommentFeedUrl() {
        assertTrue(detector.isValid(TEXTURE_COMMENT_URL));
    }
      
 
}
