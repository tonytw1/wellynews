package nz.co.searchwellington.filters

import nz.co.searchwellington.filters.attributesetters.{CombinerPageAttributeSetter, FeedAttributeSetter, PublisherPageAttributeSetter, TagPageAttributeSetter}
import nz.co.searchwellington.model.{Feed, Tag, Website}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.assertEquals
import org.junit.{Before, Test}
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.Future

class RequestFilterTest {
  @Mock private val mongoRepository: MongoRepository = null
  @Mock private val tagDAO: TagDAO = null
  @Mock private val transportTag: Tag = null
  @Mock private val soccerTag: Tag = null
  @Mock private val capitalTimesPublisher: Website = null
  @Mock private val feed: Feed = null
  private val filters = Array[RequestAttributeFilter](new PageParameterFilter)
  private var filter: RequestFilter = null

  @Before
  @throws[Exception]
  def setUp(): Unit = {
    MockitoAnnotations.initMocks(this)
    when(mongoRepository.getTagByUrlWords("transport")).thenReturn(Future.successful(Some(transportTag)))
    when(mongoRepository.getTagByUrlWords("soccer")).thenReturn(Future.successful(Some(soccerTag)))
    when(mongoRepository.getWebsiteByUrlwords("capital-times")).thenReturn(Future.successful(Some(capitalTimesPublisher)))
    when(mongoRepository.getFeedByUrlwords("tranz-metro-delays")).thenReturn(Future.successful(Some(feed)))
    filter = new RequestFilter(new CombinerPageAttributeSetter(tagDAO, mongoRepository), new PublisherPageAttributeSetter(mongoRepository),
      new FeedAttributeSetter(mongoRepository), new TagPageAttributeSetter(tagDAO, mongoRepository), filters) // TODO suggests test coverage at wrong level
  }

  @Test
  def shouldPopulateTagForAutotagUrl(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport/autotag")
    filter.loadAttributesOntoRequest(request)
    verify(mongoRepository).getTagByUrlWords("transport") // TODO don't verify your stubs
  }

  @Test
  def shouldParsePageAttribute(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport")
    request.setParameter("page", "3")

    filter.loadAttributesOntoRequest(request)

    val page = request.getAttribute("page").asInstanceOf[Integer]
    assertEquals(3, page.intValue)
  }

  @Test
  def shouldNotAttemptToResolveTagForReservedUrlWordComment(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/comment")
    filter.loadAttributesOntoRequest(request)
    verifyNoMoreInteractions(mongoRepository)
  }

  @Test
  def shouldNotAttemptToResolveTagForReservedUrlWordGeotagged(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/geotagged/rss")
    filter.loadAttributesOntoRequest(request)
    verifyNoMoreInteractions(mongoRepository)
  }

  @Test
  def shouldPopulateTagForSingleTagCommentRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport/comment")
    filter.loadAttributesOntoRequest(request)
    verify(mongoRepository).getTagByUrlWords("transport") // TODO don't verify stubs
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  def shouldPopulateTagForSingleTagCommentRssRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport/comment/rss")
    filter.loadAttributesOntoRequest(request)
    verify(mongoRepository).getTagByUrlWords("transport")
  }

  @Test
  def shouldPopulatePublisherForPublisherRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/capital-times")
    when(mongoRepository.getTagByUrlWords("capital-times")).thenReturn(Future.successful(None))

    filter.loadAttributesOntoRequest(request)

    verify(mongoRepository).getWebsiteByUrlwords("capital-times")
    assertEquals(capitalTimesPublisher, request.getAttribute("publisher"))
  }

  @Test
  def shouldPopulateTagForSingleTagGeotagRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport/geotagged")
    filter.loadAttributesOntoRequest(request)
    verify(mongoRepository).getTagByUrlWords("transport")
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  def shouldPopulateTagForSingleTagRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport")
    filter.loadAttributesOntoRequest(request)
    verify(mongoRepository).getTagByUrlWords("transport")
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  @throws[Exception]
  def shouldPopulateTagForSingleTagRssRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport/rss")

    filter.loadAttributesOntoRequest(request)

    verify(mongoRepository).getTagByUrlWords("transport")
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  @throws[Exception]
  def shouldPopulateAttributesForPublisherTagCombinerRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/capital-times+soccer")
    when(mongoRepository.getTagByUrlWords("capital-times+soccer")).thenReturn(Future.successful(None)) // TODO tag combiner pattern should have been blocked before here
    when(mongoRepository.getWebsiteByUrlwords("capital-times+soccer")).thenReturn(Future.successful(None))

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
    request.setPathInfo("/capital-times+soccer/rss")
    when(mongoRepository.getTagByUrlWords("capital-times+soccer")).thenReturn(Future.successful(None)) // TODO tag combiner pattern should have been blocked before here
    when(mongoRepository.getWebsiteByUrlwords("capital-times+soccer")).thenReturn(Future.successful(None))

    filter.loadAttributesOntoRequest(request)

    val publisher = request.getAttribute("publisher").asInstanceOf[Website]
    assertEquals(capitalTimesPublisher, publisher)
    val tag = request.getAttribute("tag").asInstanceOf[Tag]
    assertEquals(soccerTag, tag)
  }

  @Test
  @SuppressWarnings(Array("unchecked"))
  @throws[Exception]
  def shouldPopulateTagsForTagCombinerRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport+soccer")
    when(mongoRepository.getTagByUrlWords("transport+soccer")).thenReturn(Future.successful(None)) // TODO tag combiner pattern should have been blocked before here
    when(mongoRepository.getWebsiteByUrlwords("transport+soccer")).thenReturn(Future.successful(None))

    when(mongoRepository.getTagByUrlWords("transport")).thenReturn(Future.successful(Some(transportTag)))
    when(mongoRepository.getWebsiteByUrlwords("transport")).thenReturn(Future.successful(None))
    when(mongoRepository.getWebsiteByUrlwords("transport+soccer")).thenReturn(Future.successful(None))

    filter.loadAttributesOntoRequest(request)

    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    assertEquals(2, tags.size)
    assertEquals(transportTag, tags(0))
    assertEquals(soccerTag, tags(1))
  }

  @Test
  @SuppressWarnings(Array("unchecked"))
  @throws[Exception]
  def shouldPopulateTagsForTagCombinerJSONRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport+soccer/json")
    when(mongoRepository.getTagByUrlWords("transport+soccer")).thenReturn(Future.successful(None)) // TODO tag combiner pattern should have been blocked before here
    when(mongoRepository.getWebsiteByUrlwords("transport+soccer")).thenReturn(Future.successful(None))
    when(mongoRepository.getWebsiteByUrlwords("transport")).thenReturn(Future.successful(None))

    filter.loadAttributesOntoRequest(request)

    val tags = request.getAttribute("tags").asInstanceOf[Seq[Tag]]
    assertEquals(2, tags.size)
    assertEquals(transportTag, tags(0))
    assertEquals(soccerTag, tags(1))
  }

  // TODO implement
  @throws[Exception]
  def testShouldPopulateWebsiteResourceByUrlStub(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/edit/edit")
    request.setParameter("resource", "a-publisher")
    when(mongoRepository.getWebsiteByUrlwords("a-publisher")).thenReturn(Future.successful(Some(capitalTimesPublisher)))
    filter.loadAttributesOntoRequest(request)
    assertEquals(capitalTimesPublisher, request.getAttribute("resource"))
  }
}
