package nz.co.searchwellington.filters

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.filters.attributesetters._
import nz.co.searchwellington.geocoding.osm.{GeoCodeService, OsmIdParser}
import nz.co.searchwellington.model.{Feed, Tag, Website}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.{BeforeEach, Test}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{mock, verify, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class RequestFilterTest extends ReasonableWaits {

  private val transportTag = mock(classOf[Tag])
  private val soccerTag = mock(classOf[Tag])
  private val capitalTimesPublisher = Website(title = "Capital times")
  private val feed = mock(classOf[Feed])

  private val mongoRepository = mock(classOf[MongoRepository])
  private val tagDAO = mock(classOf[TagDAO])

  private val filter = new RequestFilter(new CombinerPageAttributeSetter(mongoRepository),
    new PublisherPageAttributeSetter(mongoRepository),
    new FeedAttributeSetter(mongoRepository),
    new TagPageAttributeSetter(tagDAO, mongoRepository),
    new PageParameterFilter,
    new LocationParameterFilter(mock(classOf[GeoCodeService]), new OsmIdParser)) // TODO suggests test coverage at wrong level

  @BeforeEach
  def setUp(): Unit = {
    when(mongoRepository.getTagByUrlWords("transport")).thenReturn(Future.successful(Some(transportTag)))
    when(mongoRepository.getTagByUrlWords("soccer")).thenReturn(Future.successful(Some(soccerTag)))
    when(mongoRepository.getWebsiteByUrlwords("capital-times")).thenReturn(Future.successful(Some(capitalTimesPublisher)))
    when(mongoRepository.getFeedByUrlwords("tranz-metro-delays")).thenReturn(Future.successful(Some(feed)))
    when(mongoRepository.getWebsiteByUrlwords(any())(any())).thenReturn(Future.successful(None))
  }

  @Test
  def shouldPopulateTagForAutotagUrl(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport/autotag")

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    assertEquals(transportTag, attributes("tag"))
  }

  @Test
  def shouldParsePageAttribute(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport")
    request.setParameter("page", "3")

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    assertEquals(3, attributes("page").asInstanceOf[Integer])
  }

  @Test
  def shouldPopulateTagForSingleTagCommentRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport/comment")

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    assertEquals(transportTag, attributes("tag"))
  }

  @Deprecated
  @Test
  def shouldPopulateTagForSingleTagCommentRssRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport/comment/rss")

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    verify(mongoRepository).getTagByUrlWords("transport")
    assertEquals(transportTag, attributes("tag"))
  }

  @Test
  def shouldPopulatePublisherForPublisherRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/capital-times")
    when(mongoRepository.getTagByUrlWords("capital-times")).thenReturn(Future.successful(None))
    when(mongoRepository.getWebsiteByUrlwords("capital-times")).thenReturn(Future.successful(Some(capitalTimesPublisher)))

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    assertEquals(capitalTimesPublisher, attributes("publisher"))
  }

  @Test
  def shouldPopulateTagForSingleTagGeotagRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport/geotagged")

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    assertEquals(transportTag, attributes("tag"))
  }

  @Test
  def shouldPopulateTagForSingleTagRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport")

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    assertEquals(transportTag, attributes("tag"))
  }

  @Test
  @throws[Exception]
  def shouldPopulateTagForSingleTagRssRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport/rss")

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    assertEquals(transportTag, attributes("tag"))
  }

  @Test
  def shouldPopulateAttributesForPublisherTagCombinerRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/capital-times+soccer")
    when(mongoRepository.getTagByUrlWords("capital-times")).thenReturn(Future.successful(None))
    when(mongoRepository.getWebsiteByUrlwords("capital-times")).thenReturn(Future.successful(Some(capitalTimesPublisher)))

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    val publisher = attributes("publisher").asInstanceOf[Website]
    val tag = attributes("tag").asInstanceOf[Tag]
    assertEquals(capitalTimesPublisher, publisher)
    assertEquals(soccerTag, tag)
  }

  @Test
  @throws[Exception]
  def shouldPopulateAttributesForPublisherTagCombinerRssRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/capital-times+soccer/rss")
    when(mongoRepository.getTagByUrlWords("capital-times")).thenReturn(Future.successful(None))
    when(mongoRepository.getWebsiteByUrlwords("capital-times")).thenReturn(Future.successful(Some(capitalTimesPublisher)))

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    val publisher = attributes("publisher").asInstanceOf[Website]
    assertEquals(capitalTimesPublisher, publisher)
    val tag = attributes("tag").asInstanceOf[Tag]
    assertEquals(soccerTag, tag)
  }

  @Test
  def shouldPopulateTagsForTagCombinerRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport+soccer")
    when(mongoRepository.getTagByUrlWords("transport")).thenReturn(Future.successful(Some(transportTag)))
    when(mongoRepository.getWebsiteByUrlwords("transport")).thenReturn(Future.successful(None))
    when(mongoRepository.getWebsiteByUrlwords("transport+soccer")).thenReturn(Future.successful(None))

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    val tags = attributes("tags").asInstanceOf[Seq[Tag]]
    assertEquals(2, tags.size)
    assertEquals(transportTag, tags.head)
    assertEquals(soccerTag, tags(1))
  }

  @Test
  def shouldPopulateTagsForTagCombinerJSONRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport+soccer/json")
    when(mongoRepository.getWebsiteByUrlwords("transport")).thenReturn(Future.successful(None))

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    val tags = attributes("tags").asInstanceOf[Seq[Tag]]
    assertEquals(2, tags.size)
    assertEquals(transportTag, tags.head)
    assertEquals(soccerTag, tags(1))
  }

  // TODO implement
  def testShouldPopulateWebsiteResourceByUrlStub(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/edit/edit")
    when(mongoRepository.getWebsiteByUrlwords("a-publisher")).thenReturn(Future.successful(Some(capitalTimesPublisher)))

    val attributes = Await.result(filter.loadAttributesOntoRequest(request), TenSeconds)

    assertEquals(capitalTimesPublisher, attributes("resource"))
  }

}
