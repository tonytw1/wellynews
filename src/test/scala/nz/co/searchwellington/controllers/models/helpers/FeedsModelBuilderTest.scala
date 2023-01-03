package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.suggesteditems.SuggestedFeeditemsService
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem}
import nz.co.searchwellington.model.{DiscoveredFeed, DiscoveredFeedOccurrence, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.{BeforeEach, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import reactivemongo.api.bson.BSONObjectID

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class FeedsModelBuilderTest extends ReasonableWaits with ContentFields {
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val commonAttributesModelBuilder = new CommonAttributesModelBuilder
  private val suggestedFeeditemsService = mock(classOf[SuggestedFeeditemsService])
  private val urlBuilder = mock(classOf[UrlBuilder])

  private val loggedInUser = None
  private val adminUser = User(admin = true)

  val request = new MockHttpServletRequest

  private implicit val currentSpan: Span = Span.current()

  val modelBuilder = new FeedsModelBuilder(contentRetrievalService, suggestedFeeditemsService, urlBuilder, commonAttributesModelBuilder)

  @BeforeEach
  def setUp(): Unit = {
    request.setRequestURI("/feeds")
  }

  @Test
  def shouldBeValidForFeedsPath(): Unit = {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldPopulateMainContentWithFeeds(): Unit = {
    val feeds = Seq(FrontendFeed(id = UUID.randomUUID().toString), FrontendFeed(id = UUID.randomUUID().toString))
    when(contentRetrievalService.getFeeds(None, loggedInUser)).thenReturn(Future.successful(feeds))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertEquals(feeds.asJava, mv.get(MAIN_CONTENT))
  }

  @Test
  def shouldPopulateSecondaryContent(): Unit = {
    val suggestedFeeditems = Seq(FrontendNewsitem(id = UUID.randomUUID().toString))
    when(suggestedFeeditemsService.getSuggestionFeednewsitems(6, Some(adminUser))).thenReturn(Future.successful(suggestedFeeditems))
    val currentFeeds = Seq(FrontendFeed(id = UUID.randomUUID().toString))
    when(contentRetrievalService.getAllFeedsOrderedByLatestItemDate(Some(adminUser))).thenReturn(Future.successful(currentFeeds))
    val acceptDaysAggregation = Seq((java.time.LocalDate.of(2022, 6, 1), 12L), (java.time.LocalDate.of(2022, 5, 31), 7L))
    when(contentRetrievalService.getAcceptedDates(loggedInUser = Some(adminUser))).thenReturn(Future.successful(acceptDaysAggregation))

    val discoveredFeeditems = Seq(
      DiscoveredFeed(
        url = "http://something",
        occurrences = Seq(DiscoveredFeedOccurrence(referencedFrom = "http://somewhere", seen = DateTime.now.toDate)),
        firstSeen = DateTime.now.toDate,
        publisher = Some(BSONObjectID.generate)
      )
    )
    when(contentRetrievalService.getDiscoveredFeeds(10)).thenReturn(Future.successful(discoveredFeeditems))
    when(contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)).thenReturn(Future.successful(Seq.empty))

    val extras = Await.result(modelBuilder.populateExtraModelContent(request, Some(adminUser)), TenSeconds)

    assertEquals(suggestedFeeditems.asJava, extras.get("suggestions"))
    assertEquals(currentFeeds.asJava, extras.get("righthand_content"))
    assertEquals(discoveredFeeditems.asJava, extras.get("discovered_feeds"))
  }

}
