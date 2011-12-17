package nz.co.searchwellington.htmlparsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class RssLinkExtractorTest {
	
	final RssLinkExtractor rssLinkExtractor = new RssLinkExtractor();
	
	@Test
	public void discoveredLinksShouldBeUnescaped() throws Exception {
		final String htmlWithEncodedAutoDiscoverUrls = "<html>" +
		"<head>" +
		"<link rel=\"alternate\" type=\"application/rss+xml\" title=\"Encoded link\" href=\"http://wellington.scoop.co.nz/?feed=rss2&amp;p=34601\">" +
		"</head>" +
		"</html>";
		
		final Set<String> extractedLinks = rssLinkExtractor.extractLinks(htmlWithEncodedAutoDiscoverUrls);

		assertEquals(1, extractedLinks.size());
		assertEquals("http://wellington.scoop.co.nz/?feed=rss2&p=34601", extractedLinks.iterator().next());
	}
	
	@Test
	public void shouldGracefullyIgnoreLinksTagsWithNoHrefAttributes() throws Exception {
		final String htmlWithEncodedAutoDiscoverUrls = "<html>" +
		"<head>" +
		"<link rel=\"alternate\" type=\"application/rss+xml\" title=\"Encoded link\">" +
		"</head>" +
		"</html>";
		
		final Set<String> extractedLinks = rssLinkExtractor.extractLinks(htmlWithEncodedAutoDiscoverUrls);
		
		assertTrue(extractedLinks.isEmpty());		
	}
	
}
