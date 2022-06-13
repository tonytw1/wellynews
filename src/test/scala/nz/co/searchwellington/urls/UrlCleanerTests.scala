package nz.co.searchwellington.urls

import nz.co.searchwellington.urls.shorturls.CachingShortUrlResolverService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.{mock, verify, when}

import java.net.URI

class UrlCleanerTests {

  private val SECURE_URL = new URI("https://www.secure/rss")
  private val URL_FROM_USERLAND = " www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/?PHPSESSID=eb1ca7999e13201d6dbf02a02fffacdd  "
  private val BASIC_CLEANED_URL = new URI("http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/?PHPSESSID=eb1ca7999e13201d6dbf02a02fffacdd")
  private val EXPECTED_CLEAN_URL = "http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/"
  private val SHORT_URL = new URI("http://short.url/12234")
  private val UNCLEANED_SHORT_URL = " http://short.url/12234  "
  
  private val shortUrlResolverService = mock(classOf[CachingShortUrlResolverService])
  private val cleaner = new UrlCleaner(shortUrlResolverService)

  @BeforeEach
  def setUp(): Unit = {
    when(shortUrlResolverService.resolveUrl(BASIC_CLEANED_URL)).thenReturn(BASIC_CLEANED_URL)
    when(shortUrlResolverService.resolveUrl(SHORT_URL)).thenReturn(BASIC_CLEANED_URL)
    when(shortUrlResolverService.resolveUrl(SECURE_URL)).thenReturn(SECURE_URL)
  }

  @Test
  def shouldStripPhpSessionId(): Unit = assertEquals(EXPECTED_CLEAN_URL, cleaner.cleanSubmittedItemUrl(URL_FROM_USERLAND))

  @Test
  def shouldConsultRedirectingUrlResolvers(): Unit = {
    val cleanedUrl = cleaner.cleanSubmittedItemUrl(SHORT_URL.toURL.toExternalForm)
    verify(shortUrlResolverService).resolveUrl(SHORT_URL)
    assertEquals(EXPECTED_CLEAN_URL, cleanedUrl)
  }

  @Test
  def shouldCleanupShortUrlsBeforeResolving(): Unit = {
    val cleanedUrl = cleaner.cleanSubmittedItemUrl(UNCLEANED_SHORT_URL)
    verify(shortUrlResolverService).resolveUrl(SHORT_URL)
    assertEquals(EXPECTED_CLEAN_URL, cleanedUrl)
  }

  @Test
  def testShouldPrependHttpPrefixIfMissing(): Unit = {
    val cleanedUrl = cleaner.cleanSubmittedItemUrl(UNCLEANED_SHORT_URL)
    assertEquals(EXPECTED_CLEAN_URL, cleanedUrl)
  }

  @Test
  def testShouldAllowHttpsPrefix(): Unit = {
    val cleanedUrl = cleaner.cleanSubmittedItemUrl(SECURE_URL.toURL.toExternalForm)
    assertEquals(SECURE_URL.toURL.toExternalForm, cleanedUrl)
  }

}