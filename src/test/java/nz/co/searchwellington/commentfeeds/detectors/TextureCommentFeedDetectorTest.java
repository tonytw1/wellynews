package nz.co.searchwellington.commentfeeds.detectors;

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

public class TextureCommentFeedDetectorTest extends TestCase {

    final static URL TEXTURE_COMMENT_URL = urlOf("http://texture.co.nz/blogs/features/commentrss.aspx?PostID=6696");
    
    CommentFeedDetector detector = new TextureCommentFeedDetector();

    public void testShouldDetectBlogspotCommentFeedUrl() {
        assertTrue(detector.isValid(TEXTURE_COMMENT_URL));
    }

    private static URL urlOf(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
 
}
