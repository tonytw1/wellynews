package nz.co.searchwellington.utils;

import junit.framework.TestCase;
import nz.co.searchwellington.utils.UrlCleaner;

public class UrlCleanerTests extends TestCase {
    
    public void testShouldStripPhpSessionId() throws Exception {
       UrlCleaner cleaner = new UrlCleaner(); 
       final String capitalShakersUrl = "http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/?PHPSESSID=eb1ca7999e13201d6dbf02a02fffacdd";
       assertEquals("http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/", cleaner.cleanSubmittedItemUrl(capitalShakersUrl));        
    }
    
    
    public void testShouldConsultRedirectingUrlResolvers() throws Exception {
        // TODO implement
    }
    
}
