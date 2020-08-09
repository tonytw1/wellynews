package nz.co.searchwellington.controllers.admin

import java.util.UUID

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.feeds.FeedReader
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.{MockHttpServletRequest, MockHttpServletResponse}

import scala.concurrent.ExecutionContext.Implicits.global

class AdminFeedControllerTest {
  private val FEED_ID = UUID.randomUUID().toString
  private val adminUser = User(id ="273", admin = true)
  private val feed = Feed(id = FEED_ID, title = Some("A feed"))

  private val requestFilter = mock(classOf[AdminRequestFilter])
  private val feedReader = mock(classOf[FeedReader])
  private val urlBuilder = new UrlBuilder(new SiteInformation("", "", "", "", ""), new UrlWordsGenerator)
  private val permissionService: EditPermissionService = mock(classOf[EditPermissionService])
  private val loggedInUserFilter: LoggedInUserFilter = mock(classOf[LoggedInUserFilter])

  val controller = new AdminFeedController(requestFilter, feedReader, urlBuilder, permissionService, loggedInUserFilter)

  @Test
  def manualFeedReaderRunsShouldBeAttributedToTheUserWhoKicksThemOffAndShouldAcceptAllEvenIfNoDateIsGivenOfNotCurrent(): Unit = {
    when(loggedInUserFilter.getLoggedInUser).thenReturn(Some(adminUser))
    when(permissionService.canAcceptAllFrom(feed)).thenReturn(true)

    val request = new MockHttpServletRequest()
    request.setAttribute("feedAttribute", feed)

    controller.acceptAllFrom(request, new MockHttpServletResponse)

    Mockito.verify(feedReader).processFeed(feed, adminUser, FeedAcceptancePolicy.ACCEPT_EVEN_WITHOUT_DATES)
  }

}
