package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.taggingvotes.GeotaggingVote
import nz.co.searchwellington.model.{Geocode, Newsitem, Resource}
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingDAO, HibernateResourceDAO}
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.widgets.TagsWidgetFactory
import org.junit.Assert.{assertEquals, assertNull, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest

class NewsitemPageModelBuilderTest {

  private val VALID_NEWSITEM_PAGE_PATH = "/wellington-city-council/2010/feb/01/something-about-rates"

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val taggingReturnsOfficerService = mock(classOf[TaggingReturnsOfficerService])
  private val tagWidgetFactory = mock(classOf[TagsWidgetFactory])
  private val handTaggingDAO = mock(classOf[HandTaggingDAO])
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])

  @Mock var resourceDAO: HibernateResourceDAO = mock(classOf[HibernateResourceDAO])

  @Mock var frontendNewsitem: FrontendNewsitem = null
  @Mock var geotaggingVote: GeotaggingVote = null

  private var request: MockHttpServletRequest = null
  private var builder: NewsitemPageModelBuilder = null

  @Before
  def setUp {
    MockitoAnnotations.initMocks(this)
    builder = new NewsitemPageModelBuilder(contentRetrievalService, taggingReturnsOfficerService, tagWidgetFactory, handTaggingDAO, loggedInUserFilter, resourceDAO)
    request = new MockHttpServletRequest
    request.setPathInfo(VALID_NEWSITEM_PAGE_PATH)
  }

  @Test
  def shouldAcceptValidFormatPath {
    assertTrue(builder.isValid(request))
  }

  @Test
  def shouldShowNewsitemOnMapIfItIsGeotagged {
    val place = Geocode(address = Some("Somewhere"))
    val geotaggedNewsitem = FrontendNewsitem(id = "123", place = Some(place))
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(Some(geotaggedNewsitem))
    when(resourceDAO.loadResourceById("123")).thenReturn(None)  // TODO properly exercise mapped option branch

    val mv = builder.populateContentModel(request).get

    val geotagged = mv.getModel.get("geocoded").asInstanceOf[java.util.List[Resource]]
    assertEquals(1, geotagged.size)
    assertEquals(geotaggedNewsitem, geotagged.get(0))
  }

  @Test
  def shouldNotPopulateGeotaggedItemsIfNewsitemIsNotGeotagged {
    when(frontendNewsitem.getId).thenReturn("123")
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(Some(frontendNewsitem))
    when(resourceDAO.loadResourceById("123")).thenReturn(None)  // TODO properly exercise mapped option branch

    val mv = builder.populateContentModel(request).get

    assertNull(mv.getModel.get("geocoded"))
  }

  @Test
  def shouldDisplayGeotaggingVotes {
    val newsitem = Newsitem()
    when(frontendNewsitem.getId).thenReturn(newsitem.id)
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(Some(frontendNewsitem))
    when(resourceDAO.loadResourceById(newsitem.id)).thenReturn(Some(newsitem))
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(List(geotaggingVote))

    val mv = builder.populateContentModel(request).get

    val geotaggedVotesOnModel = mv.getModel.get("geotag_votes").asInstanceOf[java.util.List[GeotaggingVote]]
    assertEquals(1, geotaggedVotesOnModel.size)
    assertEquals(geotaggingVote, geotaggedVotesOnModel.get(0))
  }

}
