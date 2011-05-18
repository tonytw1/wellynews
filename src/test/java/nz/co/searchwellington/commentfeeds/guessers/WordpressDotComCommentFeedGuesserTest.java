package nz.co.searchwellington.commentfeeds.guessers;

import junit.framework.TestCase;

public class WordpressDotComCommentFeedGuesserTest extends TestCase {

	final String WORDPRESS_DOT_COM_URL = "http://poneke.wordpress.com/2009/06/22/troll-8/";	
	CommentFeedGuesser guesser = new WordpressDotComCommentFeedGuesser();
	
	public void testShouldDetectValidUrl() throws Exception {
		assertTrue(guesser.isValid(WORDPRESS_DOT_COM_URL));
		assertFalse(guesser.isValid("http://poneke.wordpress.com/"));	
	}
	
	public void testShouldGuessFeedUrl() throws Exception {		
		assertEquals("http://poneke.wordpress.com/2009/06/22/troll-8/feed/", guesser.guessCommentFeedUrl(WORDPRESS_DOT_COM_URL));		
	}
}
