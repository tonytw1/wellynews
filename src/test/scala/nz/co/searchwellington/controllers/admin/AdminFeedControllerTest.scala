package nz.co.searchwellington.controllers.admin

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.feeds.FeedReader
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.DateTimeZone
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.dates.DateFormatter

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class AdminFeedControllerTest {
  private val FEED_ID = UUID.randomUUID().toString
  private val adminUser = User(id ="273", admin = true)
  private val feed = Feed(id = FEED_ID, title = "A feed")

  private val requestFilter = mock(classOf[AdminRequestFilter])
  private val urlWordsGenerator = new UrlWordsGenerator(new DateFormatter(DateTimeZone.UTC))
  private val urlBuilder = new UrlBuilder(new SiteInformation("", "", "", "", ""), urlWordsGenerator)
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])
  private val permissionService = new EditPermissionService()
  private val feedReader = mock(classOf[FeedReader])

  private implicit val currentSpan = Span.current()

  val controller = new AdminFeedController(requestFilter, feedReader, urlBuilder, permissionService, loggedInUserFilter)

  @Test
  def manualFeedReaderRunsShouldBeAttributedToTheUserWhoKicksThemOffAndShouldAcceptAllEvenIfNoDateIsGivenOfNotCurrent(): Unit = {
    when(loggedInUserFilter.getLoggedInUser).thenReturn(Some(adminUser))

    val request = new MockHttpServletRequest()
    request.setAttribute("feedAttribute", feed)

    controller.acceptAllFrom(request)

    Mockito.verify(feedReader).processFeed(feed, adminUser, Some(FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES))
  }

}
