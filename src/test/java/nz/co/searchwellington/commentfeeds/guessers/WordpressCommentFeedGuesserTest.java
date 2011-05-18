package nz.co.searchwellington.commentfeeds.guessers;

import junit.framework.TestCase;

public class WordpressCommentFeedGuesserTest extends TestCase {
    
    public final static String WORD_PRESS_NUMBERED_POST_URL = "http://architecture.org.nz/blog/?p=28";
    public final static String WORD_PRESS_NUMBERED_POSTS_COMMENT_URL = "http://architecture.org.nz/blog/?feed=rss2&p=28";
    
    public void testShouldCorrectlyGuessWordPressCommentFeedFromNumberedUrl() throws Exception {              
        CommentFeedGuesser guesser = new WordpressCommentFeedGuesser();        
        assertTrue(guesser.isValid(WORD_PRESS_NUMBERED_POST_URL));
        assertEquals(WORD_PRESS_NUMBERED_POSTS_COMMENT_URL, guesser.guessCommentFeedUrl(WORD_PRESS_NUMBERED_POST_URL));        
    }
    
}
