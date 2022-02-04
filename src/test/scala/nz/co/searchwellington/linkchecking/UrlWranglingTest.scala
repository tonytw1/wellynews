package nz.co.searchwellington.linkchecking

import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test

import java.net.{URI, URL}

class UrlWranglingTest extends UrlWrangling {

  @Test
  def canDetermineIfUrlStringIsFullyQualified(): Unit = {
    assertTrue(isFullQualified("http://localhost"))
    assertTrue(isFullQualified("https://localhost"))
    assertTrue(isFullQualified("http://localhost/test"))
    assertTrue(isFullQualified("http://localhost/test?foo=bar"))
    assertFalse(isFullQualified("/foo"))
  }

  @Test
  def canDetermineIfUriIsFullyQualifiedUrl(): Unit = {
    assertTrue(isFullQualifiedUrl(new URI("http://localhost")))
    assertFalse(isFullQualifiedUrl(new URI("/foo")))
  }

  @Test
  def canExpandRelativeUrlsFromFullQualifiedBaseUrl(): Unit = {
    assertEquals("https://localhost/foo", expandUrlRelativeFrom("/foo", new URL("https://localhost/test")))
    assertEquals("https://localhost/foo", expandUrlRelativeFrom("https://localhost/foo", new URL("https://localhost/test")))
  }

}
