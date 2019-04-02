package nz.co.searchwellington.controllers.admin

import java.util.{Date, UUID}

import nz.co.searchwellington.filters.{AdminRequestFilter, ResourceParameterFilter, TagsParameterFilter}
import nz.co.searchwellington.model.{Feed, Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.joda.time.DateTime
import org.junit.Assert.{assertEquals, assertNotNull}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, verify, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.Future

class AdminRequestFilterTest {
  val mongoRepository = mock(classOf[MongoRepository])
  val transportTag = Tag(id = UUID.randomUUID().toString, name = "transport")
  val feed = mock(classOf[Feed])
  val resource = mock(classOf[Resource])
  val tagDAO = mock(classOf[TagDAO])
  val request = new MockHttpServletRequest

  val filter = new AdminRequestFilter(mongoRepository, tagDAO, new ResourceParameterFilter(mongoRepository), new TagsParameterFilter(tagDAO))

  @Before
  def setUp {
    when(tagDAO.loadTagByName("transport")).thenReturn(Some(transportTag))
    when(mongoRepository.getResourceById("567")).thenReturn(Future.successful(Some(resource)))
  }

  @Test
  def shouldPopulateParentTagAttribute {
    request.setPathInfo("/edit/tag/save")
    request.setParameter("parent", "transport")

    filter.loadAttributesOntoRequest(request)

    assertNotNull(request.getAttribute("parent_tag"))
  }

  @Test
  def shouldParseDateParameterIntoDateAttribute {
    request.setPathInfo("/edit/save")
    request.setParameter("date", "23 Apr 2009")

    filter.loadAttributesOntoRequest(request)

    assertNotNull(request.getAttribute("date"))
    val date: DateTime = new DateTime(request.getAttribute("date").asInstanceOf[Date])
    assertEquals(new DateTime(2009, 4, 23, 0, 0, 0, 0), date)
  }

  @Test
  def shouldPopulateResourceFromParameter {
    request.setPathInfo("/edit/edit")
    request.setParameter("resource", "567")

    filter.loadAttributesOntoRequest(request)

    assertNotNull(request.getAttribute("resource"))
    val requestResource: Resource = request.getAttribute("resource").asInstanceOf[Resource]
    assertEquals(resource, requestResource)
  }

  @Test
  def embargoDatesInFullDateTimeFormatAreAccepted {
    request.setPathInfo("/edit/save")
    request.setParameter("embargo_date", "17 dec 2011 21:12")

    filter.loadAttributesOntoRequest(request)

    val embargoDate: Date = request.getAttribute("embargo_date").asInstanceOf[Date]
    assertNotNull(embargoDate)
    assertEquals(new DateTime(2011, 12, 17, 21, 12, 0, 0).toDate, embargoDate)
  }

  @Test
  def embargoDatesWrittenInPlainTextShouldBeAccepted {
    request.setPathInfo("/edit/save")
    request.setParameter("embargo_date", "today")

    filter.loadAttributesOntoRequest(request)

    val embargoDate = request.getAttribute("embargo_date").asInstanceOf[Date]
    assertNotNull(embargoDate)
    assertEquals(DateTime.now.toDateMidnight, new DateTime(embargoDate))
  }

  @Test
  def shouldPopulateTagFromParameterAsWell {
    request.setPathInfo("/edit/tag/save")
    request.setParameter("tag", "transport")

    filter.loadAttributesOntoRequest(request)

    verify(tagDAO).loadTagByName("transport")
    val requestTag = request.getAttribute("tag").asInstanceOf[Tag]
    assertNotNull(requestTag)
    assertEquals(transportTag, requestTag)
  }

  @Test
  def shouldPopulateFeedAttributeFromParameter {
    request.setPathInfo("/edit/tag/save")
    request.setParameter("feed", "a-feed")
    when(mongoRepository.getFeedByUrlwords("a-feed")).thenReturn(Future.successful(Some(feed)))

    filter.loadAttributesOntoRequest(request)

    assertNotNull(request.getAttribute("feedAttribute"))
  }

}
