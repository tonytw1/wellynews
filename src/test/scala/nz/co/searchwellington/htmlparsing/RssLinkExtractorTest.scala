package nz.co.searchwellington.htmlparsing

import org.apache.commons.io.IOUtils
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test

class RssLinkExtractorTest {

  private val rssLinkExtractor = new RssLinkExtractor

  @Test
  def canExtractRssAutodiscoverLinksFromHtmlPage = {
    val pageWithRssLink = IOUtils.toString(this.getClass.getClassLoader.getResourceAsStream("page-with-rss-autodiscoverable-feed.html"))

    val extractedLinks = rssLinkExtractor.extractFeedLinks(pageWithRssLink)

    assertEquals(1, extractedLinks.size)
    assertEquals("/news/rss", extractedLinks.head)
  }

  @Test
  def discoveredLinksShouldBeUnescaped = {
    val htmlWithEncodedAutoDiscoverUrls = "<html>" + "<head>" + "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"Encoded link\" href=\"http://wellington.scoop.co.nz/?feed=rss2&amp;p=34601\">" + "</head>" + "</html>"

    val extractedLinks = rssLinkExtractor.extractFeedLinks(htmlWithEncodedAutoDiscoverUrls)

    assertEquals(1, extractedLinks.size)
    assertEquals("http://wellington.scoop.co.nz/?feed=rss2&p=34601", extractedLinks.head)
  }

  @Test
  def shouldGracefullyIgnoreLinksTagsWithNoHrefAttributes = {
    val htmlWithEncodedAutoDiscoverUrls = "<html>" + "<head>" + "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"Encoded link\">" + "</head>" + "</html>"

    val extractedLinks = rssLinkExtractor.extractFeedLinks(htmlWithEncodedAutoDiscoverUrls)

    assertTrue(extractedLinks.isEmpty)
  }

}