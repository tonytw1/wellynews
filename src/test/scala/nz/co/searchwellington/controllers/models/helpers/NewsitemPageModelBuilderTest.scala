package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.taggingvotes.GeotaggingVote
import nz.co.searchwellington.model.{Geocode, Newsitem, Resource}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingDAO}
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.widgets.TagsWidgetFactory
import org.junit.Assert.{assertEquals, assertNull, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.Future

class NewsitemPageModelBuilderTest {

  private val VALID_NEWSITEM_PAGE_PATH = "/wellington-city-council/2010/feb/01/something-about-rates"

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val taggingReturnsOfficerService = mock(classOf[TaggingReturnsOfficerService])
  private val tagWidgetFactory = mock(classOf[TagsWidgetFactory])
  private val handTaggingDAO = mock(classOf[HandTaggingDAO])
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])
  private val mongoRepository = mock(classOf[MongoRepository])

  private val request = {
    val request = new MockHttpServletRequest
    request.setPathInfo(VALID_NEWSITEM_PAGE_PATH)
    request
  }

  private val builder = new NewsitemPageModelBuilder(contentRetrievalService, taggingReturnsOfficerService,
    tagWidgetFactory, handTaggingDAO, loggedInUserFilter, mongoRepository)

  @Test
  def shouldAcceptValidFormatPath {
    assertTrue(builder.isValid(request))
  }

  @Test
  def shouldShowNewsitemOnMapIfItIsGeotagged {
    val place = Geocode(address = Some("Somewhere"))
    val geotaggedNewsitem = FrontendNewsitem(id = "123", place = Some(place))
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(Some(geotaggedNewsitem))
    when(mongoRepository.getResourceById("123")).thenReturn(Future.successful(None)) // TODO properly exercise mapped option branch

    val mv = builder.populateContentModel(request).get

    val geotagged = mv.getModel.get("geocoded").asInstanceOf[java.util.List[Resource]]
    assertEquals(1, geotagged.size)
    assertEquals(geotaggedNewsitem, geotagged.get(0))
  }

  @Test
  def shouldNotPopulateGeotaggedItemsIfNewsitemIsNotGeotagged {
    val frontendNewsitem = FrontendNewsitem(id = UUID.randomUUID().toString)
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(Some(frontendNewsitem))
    when(mongoRepository.getResourceById(frontendNewsitem.id)).thenReturn(Future.successful(None)) // TODO properly exercise mapped option branch

    val mv = builder.populateContentModel(request).get

    assertNull(mv.getModel.get("geocoded"))
  }

  @Test
  def shouldDisplayGeotaggingVotes {
    val geotaggingVote = mock(classOf[GeotaggingVote])

    val newsitem = Newsitem()
    val frontendNewsitem = FrontendNewsitem(id = newsitem.id)
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(Some(frontendNewsitem))
    when(mongoRepository.getResourceById(newsitem.id)).thenReturn(Future.successful(Some(newsitem)))
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(List(geotaggingVote))

    val mv = builder.populateContentModel(request).get

    val geotaggedVotesOnModel = mv.getModel.get("geotag_votes").asInstanceOf[java.util.List[GeotaggingVote]]
    assertEquals(1, geotaggedVotesOnModel.size)
    assertEquals(geotaggingVote, geotaggedVotesOnModel.get(0))
  }

}
