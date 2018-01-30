package nz.co.searchwellington.filters

import org.junit.Assert.assertEquals
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.when
import java.util
import nz.co.searchwellington.filters.attributesetters.CombinerPageAttributeSetter
import nz.co.searchwellington.filters.attributesetters.FeedAttributeSetter
import nz.co.searchwellington.filters.attributesetters.PublisherPageAttributeSetter
import nz.co.searchwellington.filters.attributesetters.TagPageAttributeSetter
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.Website
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.repositories.TagDAO
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.mock.web.MockHttpServletRequest

class RequestFilterTest {
  @Mock private val resourceDAO: HibernateResourceDAO = null
  @Mock private val tagDAO: TagDAO = null
  @Mock private val transportTag: Tag = null
  @Mock private val soccerTag: Tag = null
  @Mock private val capitalTimesPublisher: Website = null
  @Mock private val feed: Feed = null
  private val filters = Array[RequestAttributeFilter]()
  private var filter: RequestFilter = null

  @Before
  @throws[Exception]
  def setUp(): Unit = {
    MockitoAnnotations.initMocks(this)
    when(tagDAO.loadTagByName("transport")).thenReturn(Some(transportTag))
    when(tagDAO.loadTagByName("soccer")).thenReturn(Some(soccerTag))
    when(resourceDAO.getPublisherByUrlWords("capital-times")).thenReturn(Some(capitalTimesPublisher))
    when(resourceDAO.loadFeedByUrlWords("tranz-metro-delays")).thenReturn(feed)
    filter = new RequestFilter(new CombinerPageAttributeSetter(tagDAO, resourceDAO), new PublisherPageAttributeSetter(resourceDAO), new FeedAttributeSetter(resourceDAO), new TagPageAttributeSetter(tagDAO), filters) // TODO suggests test coverage at wrong level
  }

  @Test
  @throws[Exception]
  def shouldPopulateTagForAutotagUrl(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport/autotag")
    filter.loadAttributesOntoRequest(request)
    verify(tagDAO).loadTagByName("transport")
  }

  @throws[Exception]
  def shouldParsePageAttribute(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport")
    request.setParameter("page", "3")
    filter.loadAttributesOntoRequest(request)
    val page = request.getAttribute("page").asInstanceOf[Integer]
    assertEquals(3, page.intValue)
  }

  @Test
  @throws[Exception]
  def shouldNotAttemptToResolveTagForReservedUrlWordComment(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/comment")
    filter.loadAttributesOntoRequest(request)
    verifyNoMoreInteractions(resourceDAO)
  }

  @Test
  @throws[Exception]
  def shouldNotAttemptToResolveTagForReservedUrlWordGeotagged(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/geotagged/rss")
    filter.loadAttributesOntoRequest(request)
    verifyNoMoreInteractions(resourceDAO)
  }

  @Test
  @throws[Exception]
  def shouldPopulateTagForSingleTagCommentRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport/comment")
    filter.loadAttributesOntoRequest(request)
    verify(tagDAO).loadTagByName("transport")
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  @throws[Exception]
  def shouldPopulateTagForSingleTagCommentRssRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport/comment/rss")
    filter.loadAttributesOntoRequest(request)
    verify(tagDAO).loadTagByName("transport")
  }

  @Test
  @throws[Exception]
  def shouldPopulatePublisherForPublisherRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/capital-times")
    when(tagDAO.loadTagByName("capital-times")).thenReturn(None)

    filter.loadAttributesOntoRequest(request)

    verify(resourceDAO).getPublisherByUrlWords("capital-times")
    assertEquals(capitalTimesPublisher, request.getAttribute("publisher"))
  }

  @Test
  @throws[Exception]
  def shouldPopulateTagForSingleTagGeotagRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport/geotagged")
    filter.loadAttributesOntoRequest(request)
    verify(tagDAO).loadTagByName("transport")
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  @throws[Exception]
  def shouldPopulateTagForSingleTagRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport")
    filter.loadAttributesOntoRequest(request)
    verify(tagDAO).loadTagByName("transport")
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  @throws[Exception]
  def shouldPopulateTagForSingleTagRssRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/transport/rss")

    filter.loadAttributesOntoRequest(request)

    verify(tagDAO).loadTagByName("transport")
    assertEquals(transportTag, request.getAttribute("tag"))
  }

  @Test
  @throws[Exception]
  def shouldPopulateAttributesForPublisherTagCombinerRequest(): Unit = {
    val request = new MockHttpServletRequest
    request.setPathInfo("/capital-times+soccer")
    when(tagDAO.loadTagByName("capital-times+soccer")).thenReturn(None) // TODO tag combiner pattern should have been blocked before here
    when(resourceDAO.getPublisherByUrlWords("capital-times+soccer")).thenReturn(None)

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
    when(tagDAO.loadTagByName("capital-times+soccer")).thenReturn(None) // TODO tag combiner pattern should have been blocked before here
    when(resourceDAO.getPublisherByUrlWords("capital-times+soccer")).thenReturn(None)

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
    when(tagDAO.loadTagByName("transport+soccer")).thenReturn(None) // TODO tag combiner pattern should have been blocked before here
    when(resourceDAO.getPublisherByUrlWords("transport+soccer")).thenReturn(None)

    when(tagDAO.loadTagByName("transport")).thenReturn(Some(transportTag))
    when(resourceDAO.getPublisherByUrlWords("transport")).thenReturn(None)
    when(resourceDAO.getPublisherByUrlWords("transport+soccer")).thenReturn(None)

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
    when(tagDAO.loadTagByName("transport+soccer")).thenReturn(None) // TODO tag combiner pattern should have been blocked before here
    when(resourceDAO.getPublisherByUrlWords("transport+soccer")).thenReturn(None)
    when(resourceDAO.getPublisherByUrlWords("transport")).thenReturn(None)

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
    when(resourceDAO.getPublisherByUrlWords("a-publisher")).thenReturn(Some(capitalTimesPublisher))
    filter.loadAttributesOntoRequest(request)
    assertEquals(capitalTimesPublisher, request.getAttribute("resource"))
  }
}