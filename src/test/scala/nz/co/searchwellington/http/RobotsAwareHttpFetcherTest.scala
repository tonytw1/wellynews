package nz.co.searchwellington.http

import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, verify, verifyNoMoreInteractions, when}

class RobotsAwareHttpFetcherTest {

  private val TEST_URL = "http://test.wellington.gen.nz/blah"
  private val TEST_USER_AGENT = "test user agent"

  private val robotExclusionService = mock(classOf[RobotExclusionService])
  private val httpFetcher = mock(classOf[HttpFetcher])

  private val robotsAwareFetcher = new RobotsAwareHttpFetcher(robotExclusionService, httpFetcher, Seq.empty.toArray)

  @Before
  def setup(): Unit = {
    when(httpFetcher.getUserAgent).thenReturn(TEST_USER_AGENT)
  }

  @Test
  def testMustCheckWithTheRobotsExclutionServiceBeforeCrawlingUrl(): Unit = {
    robotsAwareFetcher.httpFetch(TEST_URL)
    verify(robotExclusionService).isUrlCrawlable(TEST_URL, TEST_USER_AGENT)
  }

  @Test
  def testShouldCrawlIfFetcherSaysOk(): Unit = {
    when(robotExclusionService.isUrlCrawlable(TEST_URL, TEST_USER_AGENT)).thenReturn(true)
    robotsAwareFetcher.httpFetch(TEST_URL)
    verify(httpFetcher).httpFetch(TEST_URL)
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
    val result = robotsAwareFetcher.httpFetch(TEST_URL)
    assertEquals(401, result.status)
  }

}