package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.model.AcceptedDay
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class AcceptedModelBuilderTest extends ReasonableWaits with CommonSizes {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = mock(classOf[RssUrlBuilder])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])

  private val builder = new AcceptedModelBuilder(contentRetrievalService, rssUrlBuilder, urlBuilder, commonAttributesModelBuilder)

  @Test
  def shouldIncludeLinkOfAcceptedCountsForRecentDays(): Unit = {
    when(contentRetrievalService.getLatestNewsitems(maxItems = 5, loggedInUser = None)).thenReturn(Future.successful(Seq.empty))
    val acceptDaysAggregation = Seq(("2022-06-01", 12L),("2022-05-31", 7L))
    when(contentRetrievalService.getAcceptedDates(loggedInUser = None)).thenReturn(Future.successful(acceptDaysAggregation))

    val extras = Await.result(builder.populateExtraModelContent(new MockHttpServletRequest(), None), TenSeconds)

    val acceptedDays = extras.get("acceptedDays").asInstanceOf[java.util.List[AcceptedDay]]
    assertTrue(Option(acceptedDays).nonEmpty)
    assertEquals(AcceptedDay(LocalDate.of(2022, 6, 1), 12), acceptedDays.get(0))
  }

}
