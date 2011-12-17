package nz.co.searchwellington.commentfeeds.detectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WellingtonScoopCommentFeedDetectorTest {

	final static String SITE_FEED = "http://wellington.scoop.co.nz/?feed=rss2";	// TODO Check that this is the currently live scoop feed url.
    final static String COMMENT_FEED = "http://wellington.scoop.co.nz/?feed=rss2&p=34601";
    
    CommentFeedDetector detector = new WellingtonScoopCommentFeedDetector();
        
    @Test
    public void testShouldIgnoreSiteFeed() throws Exception {       
    	assertFalse(detector.isValid(SITE_FEED));
    }
    
    @Test
    public void testShouldDetectCommentFeedUrl() throws Exception {
        assertTrue(detector.isValid(COMMENT_FEED));
    }
    
}
