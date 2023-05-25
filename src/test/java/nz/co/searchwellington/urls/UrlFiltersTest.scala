package nz.co.searchwellington.urls

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

import java.net.URL

class UrlFiltersTest {
  @Test
  def canDetectHttpPrefixes(): Unit = {
    assertTrue(UrlFilters.hasHttpPrefix("http://blah"))
    assertFalse(UrlFilters.hasHttpPrefix("blah"))
    assertTrue(UrlFilters.hasHttpPrefix("https://blah"))
  }

  @Test
  def canStripPHPSessionIds(): Unit = {
    assertEquals("http://www.olympicharriers.org.nz/viewresults.php?eid=335", UrlFilters.stripPhpSession(new URL("http://www.olympicharriers.org.nz/viewresults.php?eid=335&PHPSESSID=e68f04603e4566f796bd0d14f3e1ba26")).toExternalForm)
    assertEquals("https://www.wcn.net.nz/news/art.php?artid=6", UrlFilters.stripPhpSession(new URL("https://www.wcn.net.nz/news/art.php?artid=6&PHPSESSID=1a8c3aaa31bddf8dcff8db6566879e37")).toExternalForm)
  }

  @Test
  def canStripUTMParameters(): Unit = {
    assertEquals("https://www.example.com/page", UrlFilters.stripUTMParams(new URL("https://www.example.com/page?utm_content=buffercf3b2&utm_medium=social&utm_source=snapchat.com&utm_campaign=buffer")).toExternalForm)
  }
}