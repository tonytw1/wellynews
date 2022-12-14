package nz.co.searchwellington.filters

import nz.co.searchwellington.filters.attributesetters.{CombinerPageAttributeSetter, FeedAttributeSetter, LocationParameterFilter, PageParameterFilter, PublisherPageAttributeSetter, TagPageAttributeSetter}
import nz.co.searchwellington.model.{Feed, Tag, Website}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.{BeforeEach, Test}
import org.mockito.Mockito.{mock, verify, verifyNoMoreInteractions, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RequestFilterTest {

  private val transportTag = mock(classOf[Tag])
  private val soccerTag = mock(classOf[Tag])
  private val capitalTimesPublisher = mock(classOf[Website])
  private val feed = mock(classOf[Feed])

  private val mongoRepository = mock(classOf[MongoRepository])
  private val tagDAO = mock(classOf[TagDAO])

  private val filter = new RequestFilter(new CombinerPageAttributeSetter(mongoRepository),
    new PublisherPageAttributeSetter(mongoRepository),
    new FeedAttributeSetter(mongoRepository),
    new TagPageAttributeSetter(tagDAO, mongoRepository),
    new PageParameterFilter, mock(classOf[LocationParameterFilter])) // TODO suggests test coverage at wrong level

  @BeforeEach
  def setUp(): Unit = {
    when(mongoRepository.getTagByUrlWords("transport")).thenReturn(Future.successful(Some(transportTag)))
    when(mongoRepository.getTagByUrlWords("soccer")).thenReturn(Future.successful(Some(soccerTag)))
    when(mongoRepository.getWebsiteByUrlwords("capital-times")).thenReturn(Future.successful(Some(capitalTimesPublisher)))
    when(mongoRepository.getFeedByUrlwords("tranz-metro-delays")).thenReturn(Future.successful(Some(feed)))

  }

  @Test
  def shouldPopulateTagForAutotagUrl(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport/autotag")
    filter.loadAttributesOntoRequest(request)
    verify(mongoRepository).getTagByUrlWords("transport") // TODO don't verify your stubs
  }

  @Test
  def shouldParsePageAttribute(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport")
    request.setParameter("page", "3")

    filter.loadAttributesOntoRequest(request)

    val page = request.getAttribute("page").asInstanceOf[Integer]
    assertEquals(3, page)
  }

  @Test
  def shouldNotAttemptToResolveTagForReservedUrlWordComment(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/comment")
    filter.loadAttributesOntoRequest(request)
    verifyNoMoreInteractions(mongoRepository)
  }

  @Test
  def shouldNotAttemptToResolveTagForReservedUrlWordGeotagged(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/geotagged/rss")
    filter.loadAttributesOntoRequest(request)
    verifyNoMoreInteractions(mongoRepository)
  }

  @Test
  def shouldPopulateTagForSingleTagCommentRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport/comment")
    filter.loadAttributesOntoRequest(request)
    verify(mongoRepository).getTagByUrlWords("transport") // TODO don't verify stubs
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  def shouldPopulateTagForSingleTagCommentRssRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport/comment/rss")
    filter.loadAttributesOntoRequest(request)
    verify(mongoRepository).getTagByUrlWords("transport")
  }

  @Test
  def shouldPopulatePublisherForPublisherRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/capital-times")
    when(mongoRepository.getTagByUrlWords("capital-times")).thenReturn(Future.successful(None))

    filter.loadAttributesOntoRequest(request)

    verify(mongoRepository).getWebsiteByUrlwords("capital-times")
    assertEquals(capitalTimesPublisher, request.getAttribute("publisher"))
  }

  @Test
  def shouldPopulateTagForSingleTagGeotagRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport/geotagged")
    filter.loadAttributesOntoRequest(request)
    verify(mongoRepository).getTagByUrlWords("transport")
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  def shouldPopulateTagForSingleTagRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport")
    filter.loadAttributesOntoRequest(request)
    verify(mongoRepository).getTagByUrlWords("transport")
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  @throws[Exception]
  def shouldPopulateTagForSingleTagRssRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport/rss")

    filter.loadAttributesOntoRequest(request)

    verify(mongoRepository).getTagByUrlWords("transport")
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  def shouldPopulateAttributesForPublisherTagCombinerRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/capital-times+soccer")
    when(mongoRepository.getTagByUrlWords("capital-times+soccer")).thenReturn(Future.successful(None)) // TODO tag combiner pattern should have been blocked before here
    when(mongoRepository.getWebsiteByUrlwords("capital-times+soccer")).thenReturn(Future.successful(None))
    when(mongoRepository.getTagByUrlWords("capital-times")).thenReturn(Future.successful(None))

    filter.loadAttributesOntoRequest(request)

    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    assertEquals(capitalTimesPublisher, publisher)
    assertEquals(soccerTag, tag)
  }

  @Test
  @throws[Exception]
  def shouldPopulateAttributesForPublisherTagCombinerRssRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/capital-times+soccer/rss")
    when(mongoRepository.getTagByUrlWords("capital-times+soccer")).thenReturn(Future.successful(None)) // TODO tag combiner pattern should have been blocked before here
    when(mongoRepository.getWebsiteByUrlwords("capital-times+soccer")).thenReturn(Future.successful(None))
    when(mongoRepository.getTagByUrlWords("capital-times")).thenReturn(Future.successful(None))

    filter.loadAttributesOntoRequest(request)

    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    assertEquals(capitalTimesPublisher, publisher)
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    assertEquals(soccerTag, tag)
  }

  @Test
  def shouldPopulateTagsForTagCombinerRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport+soccer")
    when(mongoRepository.getTagByUrlWords("transport+soccer")).thenReturn(Future.successful(None)) // TODO tag combiner pattern should have been blocked before here
    when(mongoRepository.getWebsiteByUrlwords("transport+soccer")).thenReturn(Future.successful(None))

    when(mongoRepository.getTagByUrlWords("transport")).thenReturn(Future.successful(Some(transportTag)))
    when(mongoRepository.getWebsiteByUrlwords("transport")).thenReturn(Future.successful(None))
    when(mongoRepository.getWebsiteByUrlwords("transport+soccer")).thenReturn(Future.successful(None))

    filter.loadAttributesOntoRequest(request)

    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    assertEquals(2, tags.size)
    assertEquals(transportTag, tags.head)
    assertEquals(soccerTag, tags(1))
  }

  @Test
  def shouldPopulateTagsForTagCombinerJSONRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/transport+soccer/json")
    when(mongoRepository.getTagByUrlWords("transport+soccer")).thenReturn(Future.successful(None)) // TODO tag combiner pattern should have been blocked before here
    when(mongoRepository.getWebsiteByUrlwords("transport+soccer")).thenReturn(Future.successful(None))
    when(mongoRepository.getWebsiteByUrlwords("transport")).thenReturn(Future.successful(None))

    filter.loadAttributesOntoRequest(request)

    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    assertEquals(2, tags.size)
    assertEquals(transportTag, tags.head)
    assertEquals(soccerTag, tags(1))
  }

  // TODO implement
  def testShouldPopulateWebsiteResourceByUrlStub(): Unit = {
    val request = new MockHttpServletRequest
    request.setRequestURI("/edit/edit")
    request.setParameter("resource", "a-publisher")
    when(mongoRepository.getWebsiteByUrlwords("a-publisher")).thenReturn(Future.successful(Some(capitalTimesPublisher)))
    filter.loadAttributesOntoRequest(request)
    assertEquals(capitalTimesPublisher, request.getAttribute("resource"))
  }

}
