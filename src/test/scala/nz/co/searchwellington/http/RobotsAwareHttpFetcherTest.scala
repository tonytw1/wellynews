package nz.co.searchwellington.http

import nz.co.searchwellington.ReasonableWaits
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.{mock, verify, verifyNoMoreInteractions, when}

import java.net.URL
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class RobotsAwareHttpFetcherTest extends ReasonableWaits {

  private val TEST_URL = new URL("http://test.wellington.gen.nz/blah")
  private val TEST_USER_AGENT = "test user agent"

  private val robotExclusionService = mock(classOf[RobotExclusionService])
  private val httpFetcher = mock(classOf[WSHttpFetcher])

  private val robotsAwareFetcher = new RobotsAwareHttpFetcher(robotExclusionService, httpFetcher)

  @BeforeEach
  def setup(): Unit = {
    when(httpFetcher.getUserAgent).thenReturn(TEST_USER_AGENT)
  }

  @Test
  def testMustCheckWithTheRobotsExclusionServiceBeforeCrawlingUrl(): Unit = {
    robotsAwareFetcher.httpFetch(TEST_URL)
    verify(robotExclusionService).isUrlCrawlable(TEST_URL, TEST_USER_AGENT)
  }

  @Test
  def testShouldCrawlIfFetcherSaysOk(): Unit = {
    when(robotExclusionService.isUrlCrawlable(TEST_URL, TEST_USER_AGENT)).thenReturn(true)
    robotsAwareFetcher.httpFetch(TEST_URL, true)
    verify(httpFetcher).httpFetch(TEST_URL, true)
  }

  @Test
  def testShouldNotCrawlIfFetcherSaysNo(): Unit = {
    when(robotExclusionService.isUrlCrawlable(TEST_URL, TEST_USER_AGENT)).thenReturn(false)
    robotsAwareFetcher.httpFetch(TEST_URL)
    verify(httpFetcher).getUserAgent
    verifyNoMoreInteractions(httpFetcher)
  }

  @Test
  def testShouldGiveCorrectStatusCodeIfCrawlsAreNotAllowed(): Unit = {
    when(robotExclusionService.isUrlCrawlable(TEST_URL, TEST_USER_AGENT)).thenReturn(false)

    val result = Await.result(robotsAwareFetcher.httpFetch(TEST_URL), TenSeconds)

    assertEquals(401, result.status)
  }

}