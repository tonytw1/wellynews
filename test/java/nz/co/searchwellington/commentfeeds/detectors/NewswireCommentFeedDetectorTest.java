package nz.co.searchwellington.commentfeeds.detectors;

import junit.framework.TestCase;

public class NewswireCommentFeedDetectorTest extends TestCase {

	final String NEWSWIRE_URL = "http://www.newswire.co.nz/2009/08/google-directs/feed/";
	
	public void testShouldDetectFeedUrl() throws Exception {		
		CommentFeedDetector detector = new NewswireCommentFeedDetector();
		assertTrue(detector.isValid(NEWSWIRE_URL));		
	}
	
}
