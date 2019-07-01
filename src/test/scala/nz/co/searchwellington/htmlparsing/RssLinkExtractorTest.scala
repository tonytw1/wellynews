package nz.co.searchwellington.htmlparsing

import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test

class RssLinkExtractorTest {

  private val rssLinkExtractor = new RssLinkExtractor

  @Test
  def discoveredLinksShouldBeUnescaped = {
    val htmlWithEncodedAutoDiscoverUrls = "<html>" + "<head>" + "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"Encoded link\" href=\"http://wellington.scoop.co.nz/?feed=rss2&amp;p=34601\">" + "</head>" + "</html>"

    val extractedLinks = rssLinkExtractor.extractLinks(htmlWithEncodedAutoDiscoverUrls)

    assertEquals(1, extractedLinks.size)
    assertEquals("http://wellington.scoop.co.nz/?feed=rss2&p=34601", extractedLinks.iterator.next)
  }

  @Test
  def shouldGracefullyIgnoreLinksTagsWithNoHrefAttributes = {
    val htmlWithEncodedAutoDiscoverUrls = "<html>" + "<head>" + "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"Encoded link\">" + "</head>" + "</html>"

    val extractedLinks = rssLinkExtractor.extractLinks(htmlWithEncodedAutoDiscoverUrls)

    assertTrue(extractedLinks.isEmpty)
  }
}