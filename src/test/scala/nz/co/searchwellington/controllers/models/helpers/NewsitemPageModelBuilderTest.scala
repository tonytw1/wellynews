package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.taggingvotes.GeotaggingVote
import nz.co.searchwellington.model.{Newsitem, Resource}
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingDAO, HibernateResourceDAO}
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.widgets.TagsWidgetFactory
import org.junit.Assert.{assertEquals, assertNull, assertTrue}
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.geo.model.Place

class NewsitemPageModelBuilderTest {

  private val NEWSITEM_ID = 123
  private val VALID_NEWSITEM_PAGE_PATH = "/wellington-city-council/2010/feb/01/something-about-rates"

  @Mock var contentRetrievalService: ContentRetrievalService = null
  @Mock var taggingReturnsOfficerService: TaggingReturnsOfficerService = null
  @Mock var tagWidgetFactory: TagsWidgetFactory = null
  @Mock var handTaggingDAO: HandTaggingDAO = null
  @Mock var loggedInUserFilter: LoggedInUserFilter = null
  @Mock var geotaggedNewsitem: FrontendNewsitem = null
  @Mock var frontendNewsitem: FrontendNewsitem = null
  @Mock var place: Place = null
  @Mock var resourceDAO: HibernateResourceDAO = null
  @Mock var newsitem: Newsitem = null
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
  @SuppressWarnings(Array("unchecked"))
  def shouldShowNewsitemOnMapIfItIsGeotagged {
    when(geotaggedNewsitem.getId).thenReturn(123)
    when(geotaggedNewsitem.getPlace).thenReturn(place)
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(geotaggedNewsitem)
    when(resourceDAO.loadResourceById(123)).thenReturn(None)  // TODO properly exercise mapped option branch

    val mv = builder.populateContentModel(request).get

    val geotagged: java.util.List[Resource] = mv.getModel.get("geocoded").asInstanceOf[java.util.List[Resource]]
    assertEquals(1, geotagged.size)
    assertEquals(geotaggedNewsitem, geotagged.get(0))
  }

  @Test
  def shouldNotPopulateGeotaggedItemsIfNewsitemIsNotGeotagged {
    when(frontendNewsitem.getId).thenReturn(123)
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(frontendNewsitem)
    when(resourceDAO.loadResourceById(123)).thenReturn(None)  // TODO properly exercise mapped option branch

    val mv = builder.populateContentModel(request).get

    assertNull(mv.getModel.get("geocoded"))
  }

  @Test
  def shouldDisplayGeotaggingVotes {
    when(frontendNewsitem.getId).thenReturn(123)
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(frontendNewsitem)
    when(resourceDAO.loadResourceById(NEWSITEM_ID)).thenReturn(Some(newsitem))
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(List(geotaggingVote))

    val mv = builder.populateContentModel(request).get

    val geotaggedVotesOnModel: java.util.List[GeotaggingVote] = mv.getModel.get("geotag_votes").asInstanceOf[java.util.List[GeotaggingVote]]
    assertEquals(1, geotaggedVotesOnModel.size)
    assertEquals(geotaggingVote, geotaggedVotesOnModel.get(0))
  }

}
