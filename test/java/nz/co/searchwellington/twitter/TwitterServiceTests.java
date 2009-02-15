package nz.co.searchwellington.twitter;

import junit.framework.TestCase;

import nz.co.searchwellington.twitter.TwitterService;

public class TwitterServiceTests extends TestCase {

    final String url = "http://wellington.gen.nz/url/23232";
    TwitterService twitterService = new TwitterService();
    
    public void testShouldPackageHeadlingAndUrlIntoTwit() throws Exception {        
        final String heading = "The quickbrown fox jumped over the lazy dog"; 
        assertEquals("The quickbrown fox jumped over the lazy dog - http://wellington.gen.nz/url/23232", twitterService.buildMessage(heading, url));                        
    }
    
    
    public void testShouldLimitMessagesTo140CharactersByTrimingTheHeadline() throws Exception {        
        final String longHeading = "Long message The quickbrown fox jumped over the lazy dog The quickbrown fox jumped over the lazy dog The quickbrown fox jumped over the lazy dog";        
        assertTrue(longHeading.length() + url.length() > 140);
                
        final String message = twitterService.buildMessage(longHeading, url);
        assertTrue(message.length() <= 140);        
        assertTrue(message.contains(" - " + url));       
    }
    
}
