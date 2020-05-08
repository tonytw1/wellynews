package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model.{Geocode, Newsitem, Tag, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingDAO}
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.widgets.TagsWidgetFactory
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class NewsitemPageModelBuilderTest extends ReasonableWaits {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val taggingReturnsOfficerService = mock(classOf[TaggingReturnsOfficerService])
  private val tagWidgetFactory = mock(classOf[TagsWidgetFactory])
  private val handTaggingDAO = mock(classOf[HandTaggingDAO])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private val modelBuilder = new NewsitemPageModelBuilder(contentRetrievalService, taggingReturnsOfficerService,
    tagWidgetFactory, handTaggingDAO, mongoRepository, frontendResourceMapper)

  @Test
  def isValidForSingleNewsitemPath() {
    val newsitem = Newsitem()

    val validPath = "/newsitem/" + newsitem.id
    val request = new MockHttpServletRequest
    request.setPathInfo(validPath)

    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldShowNewsitem() {
    val newsitem = Newsitem()
    val validPath = "/newsitem/" + newsitem.id
    val request = new MockHttpServletRequest
    request.setPathInfo(validPath)

    val place = Geocode(address = Some("Somewhere"))
    val frontendNewsitem = FrontendNewsitem(id = newsitem.id, place = Some(place))

    when(mongoRepository.getResourceById(newsitem.id)).thenReturn(Future.successful(Some(newsitem)))
    when(frontendResourceMapper.createFrontendResourceFrom(newsitem)).thenReturn(Future.successful(frontendNewsitem))
    when(taggingReturnsOfficerService.getHandTaggingsForResource(newsitem)).thenReturn(Future.successful(Seq.empty))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val item = mv.getModel.get("item")
    assertEquals(frontendNewsitem, item)
  }

  @Test
  def shouldShowHandTaggingsAppliedToThisNewsitem() {
    val newsitem = Newsitem()
    val validPath = "/newsitem/" + newsitem.id
    val request = new MockHttpServletRequest
    request.setPathInfo(validPath)

    val place = Geocode(address = Some("Somewhere"))
    val frontendNewsitem = FrontendNewsitem(id = newsitem.id, place = Some(place))

    val handTaggingsForNewsitem = Seq(
      HandTagging(user = User(), tag = Tag())
    )

    when(mongoRepository.getResourceById(newsitem.id)).thenReturn(Future.successful(Some(newsitem)))
    when(frontendResourceMapper.createFrontendResourceFrom(newsitem)).thenReturn(Future.successful(frontendNewsitem))
    when(taggingReturnsOfficerService.getHandTaggingsForResource(newsitem)).thenReturn(Future.successful(handTaggingsForNewsitem))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val handTaggings = mv.getModel.get("hand_taggings")
    import scala.collection.JavaConverters._
    assertEquals("Expect to be able to see all hand tagging for this newsitem", handTaggingsForNewsitem.asJava, handTaggings)
  }

  /*
  @Test
  def shouldShowNewsitemOnMapIfItIsGeotagged {
    val newsitem = Newsitem()
    val validPath = "/newsitem/" + newsitem.id
    val request = new MockHttpServletRequest
    request.setPathInfo(validPath)

    val id = UUID.randomUUID()
    val place = Geocode(address = Some("Somewhere"))
    val geotaggedNewsitem = FrontendNewsitem(id = id.toString, place = Some(place))
    when(mongoRepository.getResourceById(newsitem.id)).thenReturn(Future.successful(Some(Newsitem()))) // TODO properly exercise mapped option branch

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val geotagged = mv.getModel.get("geocoded").asInstanceOf[java.util.List[Resource]]
    assertEquals(1, geotagged.size)
    assertEquals(geotaggedNewsitem, geotagged.get(0))
  }
  */
  /*
  @Test
  def shouldNotPopulateGeotaggedItemsIfNewsitemIsNotGeotagged {
    val validPath = "/newsitem/" + Newsitem().id
    println(validPath)
    val request = new MockHttpServletRequest
    request.setPathInfo(validPath)

    val id = UUID.randomUUID()
    val frontendNewsitem = FrontendNewsitem(id = id.toString)
    when(contentRetrievalService.getNewsPage(validPath)).thenReturn(Some(frontendNewsitem))
    when(mongoRepository.getResourceById(id.toString)).thenReturn(Future.successful(Some(Newsitem()))) // TODO properly exercise mapped option branch

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertNull(mv.getModel.get("geocoded"))
  }
  */

  /*
  @Test
  def shouldDisplayGeotaggingVotes {
    val geotaggingVote = mock(classOf[GeotaggingVote])

    val newsitem = Newsitem()
    val frontendNewsitem = FrontendNewsitem(id = newsitem.id)
    when(contentRetrievalService.getNewsPage(VALID_NEWSITEM_PAGE_PATH)).thenReturn(Some(frontendNewsitem))
    when(mongoRepository.getResourceById(newsitem.id)).thenReturn(Future.successful(Some(newsitem)))
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(Future.successful(List(geotaggingVote)))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val geotaggedVotesOnModel = mv.getModel.get("geotag_votes").asInstanceOf[java.util.List[GeotaggingVote]]
    assertEquals(1, geotaggedVotesOnModel.size)
    assertEquals(geotaggingVote, geotaggedVotesOnModel.get(0))
  }
   */

}
