package nz.co.searchwellington.linkchecking

import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test

import java.net.{URI, URL}

class UrlWranglingTest extends UrlWrangling {

  @Test
  def canDetermineIfUrlStringIsFullyQualified(): Unit = {
    assertTrue(isFullQualifiedUrl(new URI("http://localhost")))
    assertTrue(isFullQualifiedUrl(new URI("https://localhost")))
    assertTrue(isFullQualifiedUrl(new URI("http://localhost/test")))
    assertTrue(isFullQualifiedUrl(new URI("http://localhost/test?foo=bar")))
    assertFalse(isFullQualifiedUrl(new URI("/foo")))
  }

  @Test
  def canDetermineIfUriIsFullyQualifiedUrl(): Unit = {
    assertTrue(isFullQualifiedUrl(new URI("http://localhost")))
    assertFalse(isFullQualifiedUrl(new URI("/foo")))
  }

  @Test
  def canExpandRelativeUrlsFromFullQualifiedBaseUrl(): Unit = {
    assertEquals("https://localhost/foo", expandUrlRelativeFrom(new URI("/foo"), new URL("https://localhost/test")).toString)
    assertEquals("https://localhost/foo", expandUrlRelativeFrom(new URI("https://localhost/foo"), new URL("https://localhost/test")).toString)
  }

}
