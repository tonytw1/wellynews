package nz.co.searchwellington.urls;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UrlParserTest {

	@Test
	public void canExtractHostFromFullyQualifiedUrl() throws Exception {		
		UrlParser parser = new UrlParser();
		
		assertEquals("wellington.gen.nz", parser.extractHostnameFrom("http://wellington.gen.nz/test/blah?q=123"));		
	}
	
}
