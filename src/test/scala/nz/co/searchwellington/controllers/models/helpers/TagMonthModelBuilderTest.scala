package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.junit.Assert.{assertFalse, assertNotNull, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class TagMonthModelBuilderTest extends ReasonableWaits with ContentFields {

  private val siteInformation = new SiteInformation("", "", "", "", "")
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = new RssUrlBuilder(siteInformation)

  private val parentTag = Tag(display_name = "Parent")
  private val tag = Tag(parent = Some(parentTag._id), display_name = "Penguins", name = "penguins")

  private val modelBuilder = new TagMonthModelBuilder(contentRetrievalService, new DateFormatter("Europe/London"), rssUrlBuilder)

  @Test
  def validForTagRequestWithMonthSuffix(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("tags", Seq(tag))
    request.setRequestURI("/" + tag.name + "/2022-apr")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def validForJsonSuffix(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("tags", Seq(tag))
    request.setRequestURI("/" + tag.name + "/2022-apr/json")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def notValidForTagRequestWithInvalidMonthSuffix(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("tags", Seq(tag))
    request.setRequestURI("/" + tag.name + "/2022-meh")
    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  def extrasIncludesNextAndPreviousMonths(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("tags", Seq(tag))
    request.setRequestURI("/" + tag.name + "/2021-feb")

    val january = new DateTime(2021, 1, 1, 0,0, 0, 0)
    val start = new DateTime(january, DateTimeZone.UTC)
    val a = ArchiveLink(count = 12L, interval = new Interval(start, start.plusMonths(1)))
    val b = ArchiveLink(count = 24L, interval = new Interval(start.plusMonths(1), start.plusMonths(2)))
    val c = ArchiveLink(count = 24L, interval = new Interval(start.plusMonths(3), start.plusMonths(3)))
    val archiveLinks = Seq(a, b, c)
    when(contentRetrievalService.getTagArchiveMonths(tag, loggedInUser = None)).thenReturn(Future.successful(archiveLinks))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    assertNotNull(extras.get("previous_month"))
    assertNotNull(extras.get("next_month"))
  }

}
