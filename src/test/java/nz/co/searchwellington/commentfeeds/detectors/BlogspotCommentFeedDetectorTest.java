package nz.co.searchwellington.commentfeeds.detectors;

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

public class BlogspotCommentFeedDetectorTest extends TestCase {

    final static URL BLOGSPOT_COMMENT_URL = urlOf("http://wellurban.blogspot.com/feeds/113750684886641660/comments/default");
    final static URL BLOGSPOT_ATOM_URL = urlOf("http://wellurban.blogspot.com/atom.xml");

    CommentFeedDetector detector = new BlogspotCommentFeedDetector();
    
    
    public void testShouldDetectBlogspotCommentFeedUrl() {
        assertTrue(detector.isValid(BLOGSPOT_COMMENT_URL));
    }
    
    
    public void testShouldIgnoreBlogspotAtomFeed() {
        assertFalse(detector.isValid(BLOGSPOT_ATOM_URL));
    }

    private static URL urlOf(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
