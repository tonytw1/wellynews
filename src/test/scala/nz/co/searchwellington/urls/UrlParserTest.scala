package nz.co.searchwellington.urls

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UrlParserTest {

  @Test
  def shouldExtractHostFromFullyQualifiedUrl(): Unit = {
    val parser = new UrlParser
    assertEquals(Some("wellington.gen.nz"), parser.extractHostnameFrom("http://wellington.gen.nz/test/blah?q=123"))
  }

}