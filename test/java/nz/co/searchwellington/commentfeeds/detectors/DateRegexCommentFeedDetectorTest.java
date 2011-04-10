package nz.co.searchwellington.commentfeeds.detectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DateRegexCommentFeedDetectorTest {
	
	private CommentFeedDetector commentFeedDetector = new DateRegexCommentFeedDetector();

	@Test
	public void shouldMatchUrlWithDateInThemAsTheseAreAlmostAlwaysCommentFeedsForSpecficPost() throws Exception {
		final String url = "http://www.blah.nz/something/2011/01/20/comments";		
		assertTrue(commentFeedDetector.isValid(url));
	}

	@Test
	public void shouldNotGiveObviousFalsePositives() throws Exception {
		final String url = "http://www.blah.nz/something/comments";		
		assertFalse(commentFeedDetector.isValid(url));
	}
	
}
