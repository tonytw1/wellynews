package nz.co.searchwellington.htmlparsing;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.junit.Test;

public class IcalLinkExtractorTest extends ExtractorTestCase {

	@Test
	public void shouldFindIcalLink() throws Exception {
		URL resource = this.getClass().getClassLoader().getResource("wellington_brass_band_front.htm");		
		File contentFile = new File(resource.getFile()); 
		StringBuffer content = loadContent(contentFile);
		
		IcalEventsLinkExtractor extractor = new IcalEventsLinkExtractor();
		Set<String> extractedLinks = extractor.extractLinks(content.toString());
		assertEquals(1, extractedLinks.size());
        assertTrue(extractedLinks.contains("http://wbb.wellington.net.nz/event/ical"));
	}
	
}
