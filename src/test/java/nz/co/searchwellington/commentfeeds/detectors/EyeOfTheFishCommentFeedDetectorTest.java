package nz.co.searchwellington.commentfeeds.detectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class EyeOfTheFishCommentFeedDetectorTest {

	final static URL SITE_FEED = urlOf("http://eyeofthefish.org/feed/");
    final static URL COMMENT_FEED = urlOf("http://eyeofthefish.org/mayor-kerry-speaks/feed/");
    
    CommentFeedDetector detector = new EyeOfTheFishCommentFeedDetector();
        
    @Test
    public void testShouldIgnoreSiteFeed() {
    	assertFalse(detector.isValid(SITE_FEED));
    }
    
    @Test
    public void testShouldDetectCommentFeedUrl() {
        assertTrue(detector.isValid(COMMENT_FEED));
    }

    private static URL urlOf(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
