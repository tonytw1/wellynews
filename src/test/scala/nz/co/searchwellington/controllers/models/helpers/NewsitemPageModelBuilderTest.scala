package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.controllers.models.ModelBuilder
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
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.geo.model.Place

class NewsitemPageModelBuilderTest {

  private val NEWSITEM_ID: Int = 123
  private val VALID_NEWSITEM_PAGE_PATH = "/wellington-city-council/2010/feb/01/something-about-rates"

  @Mock private[models] var contentRetrievalService: ContentRetrievalService = null
  @Mock private[models] var taggingReturnsOfficerService: TaggingReturnsOfficerService = null
  @Mock private[models] var tagWidgetFactory: TagsWidgetFactory = null
  @Mock private[models] var handTaggingDAO: HandTaggingDAO = null
  @Mock private[models] var loggedInUserFilter: LoggedInUserFilter = null
  @Mock private[models] var geotaggedNewsitem: FrontendNewsitem = null
  @Mock private[models] var frontendNewsitem: FrontendNewsitem = null
  @Mock private[models] var place: Place = null
  @Mock private[models] var resourceDAO: HibernateResourceDAO = null
  @Mock private[models] var newsitem: Newsitem = null
  @Mock private var geotaggingVote: GeotaggingVote = null

  private var request: MockHttpServletRequest = null
  private var builder: ModelBuilder = null

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
    when(geotaggedNewsitem.getPlace).thenReturn(place)
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(geotaggedNewsitem)

    val mv = builder.populateContentModel(request).get

    val geotagged: java.util.List[Resource] = mv.getModel.get("geocoded").asInstanceOf[java.util.List[Resource]]
    assertEquals(1, geotagged.size)
    assertEquals(geotaggedNewsitem, geotagged.get(0))
  }

  @Test
  def shouldNotPopulateGeotaggedItemsIfNewsitemIsNotGeotagged {
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(frontendNewsitem)
    val mv: ModelAndView = builder.populateContentModel(request).get
    assertNull(mv.getModel.get("geocoded"))
  }

  @Test
  def shouldDisplayGeotaggingVotes {
    when(frontendNewsitem.getId).thenReturn(123)
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(frontendNewsitem)
    when(resourceDAO.loadResourceById(NEWSITEM_ID)).thenReturn(newsitem)
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(List(geotaggingVote))

    val mv = builder.populateContentModel(request).get

    val geotaggedVotesOnModel: java.util.List[GeotaggingVote] = mv.getModel.get("geotag_votes").asInstanceOf[java.util.List[GeotaggingVote]]
    assertEquals(1, geotaggedVotesOnModel.size)
    assertEquals(geotaggingVote, geotaggedVotesOnModel.get(0))
  }

}