package nz.co.searchwellington.commentfeeds.detectors;

import junit.framework.TestCase;

public class TepapaBlogCommentFeedDetectorTest extends TestCase {

	public void testShouldDetect() throws Exception {
		final String commentFeedUrl = "http://blog.tepapa.govt.nz/2009/04/30/the-false-hen-and-chickens-fern/feed/";		
		CommentFeedDetector detector = new TepapaBlogCommentFeedDetector();
		assertTrue(detector.isValid(commentFeedUrl));
		assertFalse(detector.isValid("http://blog.tepapa.govt.nz/feed/"));
	}
	
}
