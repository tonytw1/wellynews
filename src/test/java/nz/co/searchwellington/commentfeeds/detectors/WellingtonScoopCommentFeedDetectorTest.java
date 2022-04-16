package nz.co.searchwellington.commentfeeds.detectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WellingtonScoopCommentFeedDetectorTest {

	final static String SITE_FEED = "http://wellington.scoop.co.nz/?feed=rss2";
    final static String COMMENT_FEED = "http://wellington.scoop.co.nz/?feed=rss2&p=34601";
    final static String HTTPS_COMMENT_FEED = "https://wellington.scoop.co.nz/?feed=rss2&p=34601";

    CommentFeedDetector detector = new WellingtonScoopCommentFeedDetector();
        
    @Test
    public void testShouldIgnoreSiteFeed() {
    	assertFalse(detector.isValid(SITE_FEED));
    }
    
    @Test
    public void testShouldDetectCommentFeedUrl() {
        assertTrue(detector.isValid(COMMENT_FEED));
        assertTrue(detector.isValid(HTTPS_COMMENT_FEED));
    }
    
}
