package nz.co.searchwellington.urls

import nz.co.searchwellington.urls.shorturls.CachingShortUrlResolverService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.{BeforeEach, Test}
import org.mockito.Mockito.{mock, verify, when}

import java.net.URL

class UrlCleanerTest {

  private val SECURE_URL = new URL("https://www.secure/rss")
  private val BASIC_CLEANED_URL = new URL("http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/?PHPSESSID=eb1ca7999e13201d6dbf02a02fffacdd")
  private val EXPECTED_CLEAN_URL = new URL("http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/")
  private val SHORT_URL = new URL("http://short.url/12234")

  private val shortUrlResolverService = mock(classOf[CachingShortUrlResolverService])
  private val cleaner = new UrlCleaner(shortUrlResolverService)

  {
    when(shortUrlResolverService.resolveUrl(BASIC_CLEANED_URL)).thenReturn(BASIC_CLEANED_URL)
    when(shortUrlResolverService.resolveUrl(SHORT_URL)).thenReturn(BASIC_CLEANED_URL)
    when(shortUrlResolverService.resolveUrl(SECURE_URL)).thenReturn(SECURE_URL)
  }

  @Test
  def shouldStripPhpSessionId(): Unit = assertEquals(EXPECTED_CLEAN_URL, cleaner.cleanSubmittedItemUrl(BASIC_CLEANED_URL))

  @Test
  def shouldConsultRedirectingUrlResolvers(): Unit = {
    val cleanedUrl = cleaner.cleanSubmittedItemUrl(SHORT_URL)
    verify(shortUrlResolverService).resolveUrl(SHORT_URL)
    assertEquals(EXPECTED_CLEAN_URL, cleanedUrl)
  }

  @Test
  def testShouldAllowHttpsPrefix(): Unit = {
    val cleanedUrl = cleaner.cleanSubmittedItemUrl(SECURE_URL)
    assertEquals(SECURE_URL, cleanedUrl)
  }

}