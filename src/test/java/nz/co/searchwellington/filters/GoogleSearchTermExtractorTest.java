package nz.co.searchwellington.filters;

import junit.framework.TestCase;

public class GoogleSearchTermExtractorTest extends TestCase {
	
	public void testShouldExtractSearchTerm() throws Exception {		
		final String referrer = "http://www.google.co.nz/search?hl=en&source=hp&q=wellington+galleries&meta=&aq=1&oq=Wellington+ga";
				
		GoogleSearchTermExtractor extractor = new GoogleSearchTermExtractor();
		assertEquals("wellington galleries", extractor.extractSearchTerm(referrer));
	}

}
