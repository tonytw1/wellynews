package nz.co.searchwellington.controllers.admin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import java.util.Date

import nz.co.searchwellington.filters.{AdminRequestFilter, ResourceParameterFilter, TagsParameterFilter}
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.repositories.TagDAO
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.springframework.mock.web.MockHttpServletRequest

class AdminRequestFilterTest {
  private val resourceDAO: HibernateResourceDAO = mock(classOf[HibernateResourceDAO])
  private var filter: AdminRequestFilter = null
  private val transportTag: Tag = mock(classOf[Tag])
  private val feed: Feed = mock(classOf[Feed])
  private val resource: Resource = mock(classOf[Resource])
  private val tagDAO: TagDAO = mock(classOf[TagDAO])
  private var request: MockHttpServletRequest = null

  @Before
  @throws[Exception]
  def setUp {
    when(tagDAO.loadTagByName("transport")).thenReturn(transportTag)
    when(resourceDAO.loadResourceById(567)).thenReturn(Some(resource))
    request = new MockHttpServletRequest
    filter = new AdminRequestFilter(resourceDAO, tagDAO, new ResourceParameterFilter(resourceDAO), new TagsParameterFilter(tagDAO))
  }

  @Test
  @throws[Exception]
  def testShouldPopulateParentTagAttribute {
    val request: MockHttpServletRequest = new MockHttpServletRequest
    request.setPathInfo("/edit/tag/save")
    request.setParameter("parent", "transport")
    assertNotNull(filter)
    filter.loadAttributesOntoRequest(request)
    assertNotNull(request.getAttribute("parent_tag"))
  }

  @Test
  @throws[Exception]
  def testShouldParseDateParameterIntoDateAttribute {
    request.setPathInfo("/edit/save")
    request.setParameter("date", "23 Apr 2009")
    filter.loadAttributesOntoRequest(request)
    assertNotNull(request.getAttribute("date"))
    val date: DateTime = new DateTime(request.getAttribute("date").asInstanceOf[Date])
    assertEquals(new DateTime(2009, 4, 23, 0, 0, 0, 0), date)
  }

  @Test
  @throws[Exception]
  def testShouldPopulateResourceFromParameter {
    request.setPathInfo("/edit/edit")
    request.setParameter("resource", "567")
    filter.loadAttributesOntoRequest(request)
    assertNotNull(request.getAttribute("resource"))
    val requestResource: Resource = request.getAttribute("resource").asInstanceOf[Resource]
    assertEquals(resource, requestResource)
  }

  @Test
  @throws[Exception]
  def testShouldPutTagOntoEditTagPath {
    request.setPathInfo("/edit/tag/transport")
    filter.loadAttributesOntoRequest(request)
    verify(tagDAO).loadTagByName("transport")
    val requestTag: Tag = request.getAttribute("tag").asInstanceOf[Tag]
    assertNotNull(requestTag)
  }

  @Test
  @throws[Exception]
  def testEmbargoDatesInFullDateTimeFormatAreAccepted {
    request.setPathInfo("/edit/save")
    request.setParameter("embargo_date", "17 dec 2011 21:12")
    filter.loadAttributesOntoRequest(request)
    val embargoDate: Date = request.getAttribute("embargo_date").asInstanceOf[Date]
    assertNotNull(embargoDate)
    assertEquals(new DateTime(2011, 12, 17, 21, 12, 0, 0).toDate, embargoDate)
  }

  @Test
  @throws[Exception]
  def embargoDatesWrittenInPlainTextShouldBeAccepted {
    request.setPathInfo("/edit/save")
    request.setParameter("embargo_date", "today")
    filter.loadAttributesOntoRequest(request)
    val embargoDate: Date = request.getAttribute("embargo_date").asInstanceOf[Date]
    assertNotNull(embargoDate)
    assertEquals(DateTime.now.toDateMidnight, new DateTime(embargoDate))
  }

  @Test
  @throws[Exception]
  def testShouldPopulateTagFromParameterAsWell {
    request.setPathInfo("/edit/tag/save")
    request.setParameter("tag", "transport")
    filter.loadAttributesOntoRequest(request)
    verify(tagDAO).loadTagByName("transport")
    val requestTag: Tag = request.getAttribute("tag").asInstanceOf[Tag]
    assertNotNull(requestTag)
    assertEquals(transportTag, requestTag)
  }

  @Test
  @throws[Exception]
  def testShouldPopulateFeedAttributeFromParameter {
    request.setPathInfo("/edit/tag/save")
    request.setParameter("feed", "a-feed")
    when(resourceDAO.loadFeedByUrlWords("a-feed")).thenReturn(feed)
    filter.loadAttributesOntoRequest(request)
    assertNotNull(request.getAttribute("feedAttribute"))
  }
}