package nz.co.searchwellington.commentfeeds.detectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EyeOfTheFishCommentFeedDetectorTest {

	final static String SITE_FEED = "http://eyeofthefish.org/feed/";
    final static String COMMENT_FEED = "http://eyeofthefish.org/mayor-kerry-speaks/feed/";
    
    CommentFeedDetector detector = new EyeOfTheFishCommentFeedDetector();
        
    @Test
    public void testShouldIgnoreSiteFeed() throws Exception {       
    	assertFalse(detector.isValid(SITE_FEED));
    }
    
    @Test
    public void testShouldDetectCommentFeedUrl() throws Exception {
        assertTrue(detector.isValid(COMMENT_FEED));
    }
    
}
