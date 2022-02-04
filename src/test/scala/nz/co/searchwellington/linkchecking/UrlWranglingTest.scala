package nz.co.searchwellington.linkchecking

import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test

import java.net.URL

class UrlWranglingTest extends UrlWrangling {

  @Test
  def canDetermineIfUrlIsFullyQualified(): Unit = {
    assertTrue(isFullQualified("http://localhost"))
    assertTrue(isFullQualified("https://localhost"))
    assertTrue(isFullQualified("http://localhost/test"))
    assertTrue(isFullQualified("http://localhost/test?foo=bar"))
    assertFalse(isFullQualified("/foo"))
  }

  @Test
  def canExpandRelativeUrlsFromFullQualifiedBaseUrl(): Unit = {
    assertEquals("https://localhost/foo", expandUrlRelativeFrom("/foo", new URL("https://localhost/test")))
  }

}
