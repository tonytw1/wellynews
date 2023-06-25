package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.feeds.reading.ReadFeedRequest
import nz.co.searchwellington.model._
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.queues.ReadFeedQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.DateTimeZone
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.{mock, when}
import uk.co.eelpieconsulting.common.dates.DateFormatter

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdminFeedControllerTest {
  private val FEED_ID = UUID.randomUUID().toString
  private val adminUser = User(id = "273", admin = true)

  private val urlWordsGenerator = new UrlWordsGenerator(new DateFormatter(DateTimeZone.UTC))
  private val urlBuilder = new UrlBuilder(new SiteInformation("", "", "", "", ""), urlWordsGenerator)
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])
  private val permissionService = new EditPermissionService()
  private val readFeedQueue = mock(classOf[ReadFeedQueue])
  private val mongoRepository = mock(classOf[MongoRepository])

  val controller = new AdminFeedController(readFeedQueue: ReadFeedQueue, urlBuilder, permissionService, mongoRepository, loggedInUserFilter)

  @Test
  def manualFeedReaderRunsShouldBeAttributedToTheUserWhoKicksThemOffAndShouldAcceptAllEvenIfNoDateIsGivenOfNotCurrent(): Unit = {
    val feed = Feed(id = FEED_ID, title = "A feed", url_words = Some("a-feed"), acceptance = FeedAcceptancePolicy.SUGGEST)
    when(loggedInUserFilter.getLoggedInUser).thenReturn(Some(adminUser))
    when(mongoRepository.getFeedByUrlwords("a-feed")).thenReturn(Future.successful(Some(feed)))

    controller.acceptAllFrom("a-feed")

    Mockito.verify(readFeedQueue).add(
      ReadFeedRequest(feed._id.stringify, adminUser._id.stringify, Some(FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES.toString), feed.last_read))
  }

}
