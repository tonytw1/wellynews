package nz.co.searchwellington.urls

import nz.co.searchwellington.urls.shorturls.CachingUrlResolverService
import nz.co.searchwellington.utils.UrlCleaner
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, verify, when}

class UrlCleanerTests {

  private val SECURE_URL = "https://www.secure/rss"
  private val URL_FROM_USERLAND = " www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/?PHPSESSID=eb1ca7999e13201d6dbf02a02fffacdd  "
  private val BASIC_CLEANED_URL = "http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/?PHPSESSID=eb1ca7999e13201d6dbf02a02fffacdd"
  private val EXPECTED_CLEAN_URL = "http://www.capitalshakers.co.nz/news/genesis-energy-shakers-vs-otago-rebels-easter-sunday/"
  private val SHORT_URL = "http://short.url/12234"
  private val UNCLEANED_SHORT_URL = " http://short.url/12234  "
  
  private val urlResolverService = mock(classOf[CachingUrlResolverService])
  private val cleaner = new UrlCleaner(urlResolverService)

  @Before
  def setUp(): Unit = {
    when(urlResolverService.resolveUrl(BASIC_CLEANED_URL)).thenReturn(BASIC_CLEANED_URL)
    when(urlResolverService.resolveUrl(SHORT_URL)).thenReturn(BASIC_CLEANED_URL)
    when(urlResolverService.resolveUrl(SECURE_URL)).thenReturn(SECURE_URL)
  }

  @Test
  def shouldStripPhpSessionId(): Unit = assertEquals(EXPECTED_CLEAN_URL, cleaner.cleanSubmittedItemUrl(URL_FROM_USERLAND))

  @Test
  def shouldConsultRedirectingUrlResolvers(): Unit = {
    val cleanedUrl = cleaner.cleanSubmittedItemUrl(SHORT_URL)
    verify(urlResolverService).resolveUrl(SHORT_URL)
    assertEquals(EXPECTED_CLEAN_URL, cleanedUrl)
  }

  @Test
  def shouldCleanupShortUrlsBeforeResolving(): Unit = {
    val cleanedUrl = cleaner.cleanSubmittedItemUrl(UNCLEANED_SHORT_URL)
    verify(urlResolverService).resolveUrl(SHORT_URL)
    assertEquals(EXPECTED_CLEAN_URL, cleanedUrl)
  }

  @Test
  def testShouldPreappenedHttpPrefixIfMissing(): Unit = {
    val cleanedUrl = cleaner.cleanSubmittedItemUrl(UNCLEANED_SHORT_URL)
    assertEquals(EXPECTED_CLEAN_URL, cleanedUrl)
  }

  @Test
  def testShouldAllowHttpsPrefix(): Unit = {
    val cleanedUrl = cleaner.cleanSubmittedItemUrl(SECURE_URL)
    assertEquals(SECURE_URL, cleanedUrl)
  }

}