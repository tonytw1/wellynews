package nz.co.searchwellington.htmlparsing;

import java.io.File;
import java.util.List;

public class IcalLinkExtractorTest extends ExtractorTestCase {

	public void testShouldFindIcalLink() throws Exception {		
		File contentFile = new File("test/java/nz/co/searchwellington/htmlparsing/wellington_brass_band_front.htm"); 
		StringBuffer content = loadContent(contentFile);
		
		IcalEventsLinkExtractor extractor = new IcalEventsLinkExtractor();
		List<String> extractedLinks = extractor.extractLinks(content.toString());
		assertEquals(1, extractedLinks.size());
        assertEquals("http://wbb.wellington.net.nz/event/ical", extractedLinks.get(0));
	}
}
