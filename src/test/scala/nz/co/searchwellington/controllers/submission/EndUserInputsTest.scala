package nz.co.searchwellington.controllers.submission

import nz.co.searchwellington.urls.UrlCleaner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}

import java.net.URL

class EndUserInputsTest extends EndUserInputs {

  private val WITH_BLANK_SPACES = " http://localhost/1234  "
  private val WITH_MISSING_PROTOCOL = "localhost/123"

  val urlCleaner: UrlCleaner = mock(classOf[UrlCleaner])

  @Test
  def shouldCleanupShortUrlsBeforeResolving(): Unit = {
    when(urlCleaner.cleanSubmittedItemUrl(new URL("http://localhost/1234"))).thenReturn(new URL("http://localhost/1234"))

    val cleanedUrl = cleanUrl(WITH_BLANK_SPACES)

    assertEquals("http://localhost/1234", cleanedUrl.toOption.get.toExternalForm)
  }

  @Test
  def testShouldPrependHttpPrefixIfMissing(): Unit = {
    when(urlCleaner.cleanSubmittedItemUrl(new URL("http://localhost/123"))).thenReturn(new URL("http://localhost/123"))

    val cleanedUrl = cleanUrl(WITH_MISSING_PROTOCOL)

    assertEquals("http://localhost/123", cleanedUrl.toOption.get.toExternalForm)
  }

}
