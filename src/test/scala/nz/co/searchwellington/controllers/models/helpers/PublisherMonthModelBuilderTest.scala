package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{ArchiveLink, SiteInformation, Website}
import nz.co.searchwellington.model.frontend.{FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.RssUrlBuilder
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertNotNull, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.dates.DateFormatter

import java.util.regex.Pattern
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class PublisherMonthModelBuilderTest extends ReasonableWaits with ContentFields with TestArchiveLinks {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private val publisher = Website(title = "A publisher", url_words = Some("a-publisher"))

  private val newsitem = mock(classOf[FrontendResource])
  private val anotherNewsitem = mock(classOf[FrontendResource])
  private val monthNewsitems = Seq(newsitem, anotherNewsitem)

  private implicit val currentSpan: Span = Span.current()

  private val modelBuilder = new PublisherMonthModelBuilder(contentRetrievalService, frontendResourceMapper,
    new DateFormatter(DateTimeZone.UTC), new RssUrlBuilder(new SiteInformation()))

  @Test
  def testPathMatcher(): Unit = {
    val regex = "/.*/[0-9]+-.*?"
    val pattern = Pattern.compile(regex)

    assertTrue(pattern.matcher("/wcc/2019-feb").matches())
    assertFalse(pattern.matcher("/wcc/rss").matches())
  }

  @Test
  def shouldBeValidForPublisherAndMonthPath(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("publisher", publisher)
    request.setRequestURI("/a-publisher/2020-feb")

    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldNotBeValidForPublisherAndMonthPath(): Unit = {
    val archiveMonthRequest = new MockHttpServletRequest
    archiveMonthRequest.setRequestURI("/2020-may")

    assertFalse(modelBuilder.isValid(archiveMonthRequest))
  }

  @Test
  def shouldBeValidForPublisherNonDate(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("publisher", publisher)
    request.setRequestURI("/a-publisher/something")

    assertFalse(modelBuilder.isValid(request))
  }

  @Test
  def mainContentIsPublishersNewsitemsForMonth(): Unit = {
    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)
    request.setRequestURI("/a-publisher/2020-jul")

    val july = new DateTime(2020, 7, 1, 0, 0, DateTimeZone.UTC)
    val month = new Interval(july, july.plusMonths(1))

    when(contentRetrievalService.getNewsitemsForPublisherInterval(publisher, month, None)).thenReturn(Future.successful(monthNewsitems))
    val frontendPubisher = FrontendWebsite(id = "123")
    when(frontendResourceMapper.createFrontendResourceFrom(publisher, None)).thenReturn(Future.successful(frontendPubisher))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(monthNewsitems.asJava, mv.get(MAIN_CONTENT))
    assertEquals("A publisher - July 2020", mv.get("heading"))
    assertEquals(frontendPubisher, mv.get("publisher"))
  }

  @Test
  def extrasShouldIncludePublisherArchiveLinks(): Unit = {
    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)
    request.setRequestURI("/a-publisher/2020-jul")
    val frontendPublisher = FrontendWebsite(id = "123")

    val july = new DateTime(2020, 7, 1, 0, 0)
    val monthOfJuly = new Interval(july, july.plusMonths(1))
    val archiveLinks = Seq(ArchiveLink(count = Some(2), interval = monthOfJuly))

    when(frontendResourceMapper.createFrontendResourceFrom(publisher, None)).thenReturn(Future.successful(frontendPublisher))
    when(contentRetrievalService.getPublisherArchiveMonths(publisher, None)).thenReturn(Future.successful(archiveLinks))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    assertNotNull(extras.get("archive_links"))
  }

  @Test
  def extrasIncludesNextAndPreviousMonths(): Unit = {
    val request = new MockHttpServletRequest
    request.setAttribute("publisher", publisher)
    request.setRequestURI("/a-publisher/2020-jul")
    val frontendPublisher = FrontendWebsite(id = "123")

    when(frontendResourceMapper.createFrontendResourceFrom(publisher, None)).thenReturn(Future.successful(frontendPublisher))
    when(contentRetrievalService.getPublisherArchiveMonths(publisher, None)).thenReturn(Future.successful(someArchiveMonths))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, None), TenSeconds)

    assertNotNull(extras.get("next_month"))
  }

}
