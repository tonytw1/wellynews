package nz.co.searchwellington.commentfeeds.detectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class WellingtonScoopCommentFeedDetectorTest {

	final static URL SITE_FEED = urlOf("http://wellington.scoop.co.nz/?feed=rss2");
    final static URL COMMENT_FEED =urlOf("http://wellington.scoop.co.nz/?feed=rss2&p=34601");
    final static URL HTTPS_COMMENT_FEED = urlOf("https://wellington.scoop.co.nz/?feed=rss2&p=34601");

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

    private static URL urlOf(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
