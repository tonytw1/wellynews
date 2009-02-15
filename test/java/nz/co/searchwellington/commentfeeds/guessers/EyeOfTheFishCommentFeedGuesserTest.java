package nz.co.searchwellington.commentfeeds.guessers;

import junit.framework.TestCase;

public class EyeOfTheFishCommentFeedGuesserTest extends TestCase {

    
    private static final String EYE_OF_THE_FISH_PREFIX = "http://eyeofthefish.org";
    private static final String EYE_OF_THE_FISH_POST_URL = EYE_OF_THE_FISH_PREFIX + "/test/";
    
    
    public void testShouldCorrectlyGuessEyeOfTheFishCommentFeedFromNumberedUrl() throws Exception {  
        CommentFeedGuesser guesser = new EyeOfTheFishCommentFeedGuesser();        
        assertTrue(guesser.isValid(EYE_OF_THE_FISH_POST_URL));
        
        assertFalse(guesser.isValid(EYE_OF_THE_FISH_PREFIX));
        assertFalse(guesser.isValid(EYE_OF_THE_FISH_PREFIX + "/"));
        assertEquals("http://brownbag.wellington.gen.nz/feeds/eyeofthefish-comments/test", guesser.guessCommentFeedUrl(EYE_OF_THE_FISH_POST_URL));
    }
    
    
    
}
