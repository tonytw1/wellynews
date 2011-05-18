package nz.co.searchwellington.htmlparsing;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Set;

import org.junit.Test;

public class GoogleCalandarLinkExtractorTest extends ExtractorTestCase {

    final String DOWNSTAGE_CALENDAR_LINK = "http://www.google.com/calendar/render?cid=u33l192jom2msi7sppbsu5ehgc%40group.calendar.google.com";

    @Test
    public void shouldDetectValidGoogleCalenderUrls() throws Exception {
        GoogleCalendarLinkExtractor extractor = new GoogleCalendarLinkExtractor();        
        assertTrue(extractor.isValid(DOWNSTAGE_CALENDAR_LINK));
        assertFalse(extractor.isValid("http://www.google.com/calendar/render"));
        assertFalse(extractor.isValid(null));
        assertFalse(extractor.isValid(""));
    }
	    
    @Test
	public void shouldFindCalendarLink() throws Exception {		
		File contentFile = new File("./test/java/nz/co/searchwellington/htmlparsing/downstage_news.htm"); 
		StringBuffer content = loadContent(contentFile);
		
		LinkExtractor extractor = new GoogleCalendarLinkExtractor();
		Set<String> extractedLinks = extractor.extractLinks(content.toString());
		assertEquals(1, extractedLinks.size());
		assertTrue(extractedLinks.contains("http://www.google.com/calendar/render?cid=u33l192jom2msi7sppbsu5ehgc%40group.calendar.google.com"));
	}
    
}
