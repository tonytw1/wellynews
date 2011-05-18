package nz.co.searchwellington.spam;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.WebsiteImpl;
import junit.framework.TestCase;

public class SpamFilterTest extends TestCase {
    
    SpamFilter filter = new SpamFilter();
    
    public void testAllowsNormalSubmission() throws Exception {        
        Resource okResource = new WebsiteImpl();
        okResource.setName("Test site");
        okResource.setUrl("http://www.test.com.localhost");
        okResource.setDescription("test test");        
        assertFalse(filter.isSpam(okResource));               
    }
    
    public void testShouldBlockRFID() throws Exception {        
        Resource spamResource = new WebsiteImpl();
        spamResource.setName("Test site");
        spamResource.setUrl("http://www.rfid.com");
        spamResource.setDescription("test test");      
        assertTrue(filter.isSpam(spamResource));               
    }
    
    public void testShouldBlockByDescription() throws Exception {        
        Resource spamResource = new WebsiteImpl();
        spamResource.setName("Test site");
        spamResource.setUrl("http://www.test.com.localhost");
        spamResource.setDescription("test rfid test");      
        assertTrue(filter.isSpam(spamResource));               
    }

}
