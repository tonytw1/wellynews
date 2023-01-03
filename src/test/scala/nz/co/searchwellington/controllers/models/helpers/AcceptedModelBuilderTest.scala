package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.model.AcceptedDay
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.{DateTime, LocalDate}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class AcceptedModelBuilderTest extends ReasonableWaits with CommonSizes {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])

  private val builder = new AcceptedModelBuilder(contentRetrievalService, rssUrlBuilder, urlBuilder, commonAttributesModelBuilder)

  private implicit val currentSpan: Span = Span.current()

  @Test
  def shouldIncludeLinkOfAcceptedCountsForRecentDays(): Unit = {
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))
    val acceptDaysAggregation = Seq(
      (java.time.LocalDate.of(2022, 6, 1), 12L),
      (java.time.LocalDate.of(2022, 5, 31), 7L))
    when(contentRetrievalService.getAcceptedDates(loggedInUser = None)).thenReturn(Future.successful(acceptDaysAggregation))

    val extras = Await.result(builder.populateExtraModelContent(new MockHttpServletRequest(), None), TenSeconds)

    val acceptedDays = extras.get("acceptedDays").asInstanceOf[java.util.List[AcceptedDay]]
    assertTrue(Option(acceptedDays).nonEmpty)
    assertEquals(AcceptedDay(java.time.LocalDate.of(2022, 6, 1), 12), acceptedDays.get(0))
  }

  @Test
  def shouldPaginateByAcceptedDat(): Unit = {
    val request = new MockHttpServletRequest
    val secondOfSeptember = new LocalDate(2022, 9, 2)
    request.setParameter("date", secondOfSeptember.toString)
    val acceptedOnThe2nd = Seq(FrontendNewsitem(id = UUID.randomUUID().toString, accepted = new DateTime(2022, 9, 2, 12, 23, 0).toDate))
    when(contentRetrievalService.getAcceptedNewsitems(maxItems = MAX_NEWSITEMS, loggedInUser = None, acceptedDate = Some(secondOfSeptember))).thenReturn(Future.successful(acceptedOnThe2nd, acceptedOnThe2nd.size))

    val model = Await.result(builder.populateContentModel(request, loggedInUser = None), TenSeconds).get

    assertEquals(acceptedOnThe2nd.asJava, model.get("main_content"))
  }

}
