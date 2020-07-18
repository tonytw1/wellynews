package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{DiscoveredFeed, FeedAcceptancePolicy}
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem}
import nz.co.searchwellington.repositories.{ContentRetrievalService, SuggestedFeeditemsService}
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.DateTime
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class FeedsModelBuilderTest extends ReasonableWaits with ContentFields {
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val commonAttributesModelBuilder = mock(classOf[CommonAttributesModelBuilder])
  private val suggestedFeeditemsService = mock(classOf[SuggestedFeeditemsService])
  private val urlBuilder = mock(classOf[UrlBuilder])

  private val loggedInUser = None

  var request = new MockHttpServletRequest

  val modelBuilder = new FeedsModelBuilder(contentRetrievalService, suggestedFeeditemsService, urlBuilder, commonAttributesModelBuilder)

  @Before
  def setUp {
    request.setRequestURI("/feeds")
  }

  @Test
  def isValidForFeedsPath {
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldPopulateMainContentWithFeeds {
    val feeds = Seq(FrontendFeed(id = UUID.randomUUID().toString), FrontendFeed(id = UUID.randomUUID().toString))
    when(contentRetrievalService.getFeeds(None, loggedInUser)).thenReturn(Future.successful(feeds))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    assertEquals(feeds.asJava, mv.getModel.get(MAIN_CONTENT))
  }

  @Test
  def shouldPopulateSecondaryContent = {
    val suggestedFeeditems = Seq(FrontendNewsitem(id = UUID.randomUUID().toString))
    when(suggestedFeeditemsService.getSuggestionFeednewsitems(6, None)).thenReturn(Future.successful(suggestedFeeditems))
    val discoveredFeeditems = Seq(DiscoveredFeed(url = "http://something", referencedFrom = "http://somewhere", seen = DateTime.now.toDate))
    when(contentRetrievalService.getDiscoveredFeeds).thenReturn(Future.successful(discoveredFeeditems))
    when(contentRetrievalService.getAllFeedsOrderedByLatestItemDate(loggedInUser)).thenReturn(Future.successful(Seq.empty))
    when(contentRetrievalService.getSuggestOnlyFeeds(None)).thenReturn(Future.successful(Seq.empty))
    val mv = new ModelAndView()

    Await.result(modelBuilder.populateExtraModelContent(request, mv, None), TenSeconds)

    import scala.collection.JavaConverters._
    //assertEquals(suggestedFeeditems.asJava, mv.getModel.get("suggestions"))
    assertEquals(discoveredFeeditems.asJava, mv.getModel.get("discovered_feeds"))
  }

}
