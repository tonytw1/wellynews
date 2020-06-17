package nz.co.searchwellington.controllers.admin

import java.util.UUID

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.feeds.FeedReader
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, User}
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.{Before, Test}
import org.mockito.{Mock, Mockito, MockitoAnnotations}
import org.springframework.mock.web.{MockHttpServletRequest, MockHttpServletResponse}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global

class AdminFeedControllerTest {

  private val FEED_ID = UUID.randomUUID().toString
  private val FEED_OBJECT_ID = BSONObjectID.generate()

  @Mock val requestFilter: AdminRequestFilter = null
  @Mock val feedReader: FeedReader = null
  @Mock val urlBuilder: UrlBuilder = null
  @Mock val permissionService: EditPermissionService = null
  @Mock val loggedInUserFilter: LoggedInUserFilter = null
  private var request: MockHttpServletRequest = null
  private var response: MockHttpServletResponse = null
  private val adminUser = User(id ="273", admin = true)
  private val feed = Feed(id = FEED_ID, title = Some("A feed"))

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    response = new MockHttpServletResponse
  }

  @Test
  def manualFeedReaderRunsShouldBeAttributedToTheUserWhoKicksThemOffAndShouldAcceptAllEvenIfNoDateIsGivenOfNotCurrent(): Unit = {
    Mockito.when(loggedInUserFilter.getLoggedInUser).thenReturn(Some(adminUser))
    Mockito.when(permissionService.canAcceptAllFrom(feed)).thenReturn(true)

    val controller = new AdminFeedController(requestFilter, feedReader, urlBuilder, permissionService, loggedInUserFilter)
    request.setAttribute("feedAttribute", feed)

    controller.acceptAllFrom(request, response)

    Mockito.verify(feedReader).processFeed(feed, adminUser, FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES)
  }

}
