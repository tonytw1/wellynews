package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.RssUrlBuilder
import org.joda.time.{DateTime, Interval}
import org.junit.jupiter.api.Assertions.{assertFalse, assertNotNull, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class TagMonthModelBuilderTest extends ReasonableWaits with ContentFields with TestArchiveLinks {

  private val siteInformation = new SiteInformation("", "", "", "", "")
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = new RssUrlBuilder(siteInformation)

  private val parentTag = Tag(display_name = "Parent")
  private val tag = Tag(parent = Some(parentTag._id), display_name = "Penguins", name = "penguins")

  private implicit val currentSpan: Span = Span.current()

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

    when(contentRetrievalService.getTagArchiveMonths(tag, loggedInUser = None)).thenReturn(Future.successful(someArchiveMonths))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    assertNotNull(extras.get("previous_month"))
    assertNotNull(extras.get("next_month"))
  }

  @Test
  def extrasShouldIncludeTagArchiveLinks(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("tags", Seq(tag))
    request.setRequestURI("/" + tag.name + "/2021-feb")

    val july = new DateTime(2020, 7, 1, 0, 0)
    val monthOfJuly = new Interval(july, july.plusMonths(1))
    val archiveLinks = Seq(ArchiveLink(count = Some(2), interval = monthOfJuly))

    when(contentRetrievalService.getTagArchiveMonths(tag, None)).thenReturn(Future.successful(archiveLinks))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    assertNotNull(extras.get("archive_links"))
  }

}
