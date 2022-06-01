package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.whakaoko.WhakaokoFeedReader
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.modification.ContentUpdateService
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.Mockito.mock

import scala.concurrent.ExecutionContext.Implicits.global

class FeedReaderTest {

  private val feedItemAcceptanceDecider = mock(classOf[FeedItemAcceptanceDecider])
  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val feedReaderUpdateService=  mock(classOf[FeedReaderUpdateService])
  private val whakaokoFeedReader =  mock(classOf[WhakaokoFeedReader])
  private val feeditemToNewsItemService=  mock(classOf[FeeditemToNewsitemService])

  private val suggestedFeed = Feed(acceptance = FeedAcceptancePolicy.SUGGEST)
  private val loggedInUser = User()

  private val feedReader = new FeedReader(feedItemAcceptanceDecider, contentUpdateService, feedReaderUpdateService, whakaokoFeedReader, feeditemToNewsItemService)

  @Test
  def shouldNotAcceptNewsitemsFromSuggestOnlyFeeds(): Unit = {
    feedReader.processFeed(suggestedFeed, loggedInUser)

    fail()
  }

  @Test
  def shouldUpdateLastReadAndLatestItemForSuggestOnlyFeeds(): Unit = {

    feedReader.processFeed(suggestedFeed, loggedInUser)

    fail()
  }

}