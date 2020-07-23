package nz.co.searchwellington.controllers.models.helpers

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.taggingvotes.{GeotaggingVote, HandTagging}
import nz.co.searchwellington.model.{Geocode, Newsitem, Tag, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingDAO}
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.widgets.TagsWidgetFactory
import org.junit.Assert.{assertEquals, assertTrue, assertNotNull, assertNull}
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
    request.setRequestURI(validPath)

    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldShowNewsitem() {
    val newsitem = Newsitem()
    val validPath = "/newsitem/" + newsitem.id
    val request = new MockHttpServletRequest
    request.setRequestURI(validPath)

    val place = Geocode(address = Some("Somewhere"))
    val frontendNewsitem = FrontendNewsitem(id = newsitem.id, place = Some(place))

    when(mongoRepository.getResourceById(newsitem.id)).thenReturn(Future.successful(Some(newsitem)))
    when(frontendResourceMapper.createFrontendResourceFrom(newsitem, None)).thenReturn(Future.successful(frontendNewsitem))
    when(taggingReturnsOfficerService.getHandTaggingsForResource(newsitem)).thenReturn(Future.successful(Seq.empty))
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(Future.successful(Seq.empty))
    when(taggingReturnsOfficerService.getIndexTaggingsForResource(newsitem)).thenReturn(Future.successful(Seq.empty))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val item = mv.getModel.get("item")
    assertEquals(frontendNewsitem, item)
  }

  @Test
  def shouldShowTaggingsAppliedToThisNewsitem() {
    val newsitem = Newsitem()
    val validPath = "/newsitem/" + newsitem.id
    val request = new MockHttpServletRequest
    request.setRequestURI(validPath)

    val place = Geocode(address = Some("Somewhere"))
    val frontendNewsitem = FrontendNewsitem(id = newsitem.id, place = Some(place))

    val handTaggingsForNewsitem = Seq(HandTagging(user = User(), tag = Tag()))
    val geotagVotesForNewsitem = Seq(new GeotaggingVote(geocode = place, weight = 1, explanation = "Some tagging"))
    val indexTaggingsForNewsitem = Seq(HandTagging(tag = Tag(id = "123"), user = User()))

    when(mongoRepository.getResourceById(newsitem.id)).thenReturn(Future.successful(Some(newsitem)))
    when(frontendResourceMapper.createFrontendResourceFrom(newsitem, None)).thenReturn(Future.successful(frontendNewsitem))
    when(taggingReturnsOfficerService.getHandTaggingsForResource(newsitem)).thenReturn(Future.successful(handTaggingsForNewsitem))
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(Future.successful(geotagVotesForNewsitem))
    when(taggingReturnsOfficerService.getIndexTaggingsForResource(newsitem)).thenReturn(Future.successful(indexTaggingsForNewsitem))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    import scala.collection.JavaConverters._
    val handTaggings = mv.getModel.get("hand_taggings")
    assertEquals("Expect to be able to see all hand tagging for this newsitem", handTaggingsForNewsitem.asJava, handTaggings)

    val geoTagVotes = mv.getModel.get("geotag_votes")
    assertEquals("Expect to be see geotagging votes", geotagVotesForNewsitem.asJava, geoTagVotes)

    val indexTaggings = mv.getModel.get("index_taggings")
    assertEquals("Expect to be see index taggings", indexTaggingsForNewsitem.asJava, indexTaggings)
  }

  @Test
  def shouldShowNewsitemOnMapIfItIsGeotagged {
    val id = UUID.randomUUID()
    val geotaggedNewsitem = Newsitem(id = id.toString)

    val validPath = "/newsitem/" + id.toString
    val request = new MockHttpServletRequest
    request.setRequestURI(validPath)

    val place = Geocode(address = Some("Somewhere"))
    val geotaggedFrontendNewsitem = FrontendNewsitem(id = id.toString, place = Some(place))

    when(mongoRepository.getResourceById(id.toString)).thenReturn(Future.successful(Some(geotaggedNewsitem))) // TODO properly exercise mapped option branch
    when(frontendResourceMapper.createFrontendResourceFrom(geotaggedNewsitem, None)).thenReturn(Future.successful(geotaggedFrontendNewsitem))

    when(taggingReturnsOfficerService.getHandTaggingsForResource(geotaggedNewsitem)).thenReturn(Future.successful(Seq.empty))
    when(taggingReturnsOfficerService.getIndexTaggingsForResource(geotaggedNewsitem)).thenReturn(Future.successful(Seq.empty))

    val geotaggingVote = new GeotaggingVote(place, "Publisher's location", 1)
    when(taggingReturnsOfficerService.getGeotagVotesForResource(geotaggedNewsitem)).thenReturn(Future.successful(Seq(geotaggingVote)))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val geotagged = mv.getModel.get("geocoded").asInstanceOf[java.util.List[FrontendNewsitem]]
    assertNotNull(geotagged)
    assertEquals(1, geotagged.size)
    assertEquals(geotaggedFrontendNewsitem, geotagged.get(0))
  }

  @Test
  def shouldNotPopulateGeotaggedItemsIfNewsitemIsNotGeotagged {
    val validPath = "/newsitem/" + Newsitem().id
    val request = new MockHttpServletRequest
    request.setRequestURI(validPath)

    val id = UUID.randomUUID()
    val frontendNewsitem = FrontendNewsitem(id = id.toString)
    when(mongoRepository.getResourceById(id.toString)).thenReturn(Future.successful(Some(Newsitem()))) // TODO properly exercise mapped option branch

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertNull(mv.getModel.get("geocoded"))
  }

  @Test
  def shouldDisplayGeotaggingVotes {
    val validPath = "/newsitem/" + Newsitem().id
    val request = new MockHttpServletRequest
    request.setRequestURI(validPath)

    val geotaggingVote = mock(classOf[GeotaggingVote])

    val newsitem = Newsitem()
    val frontendNewsitem = FrontendNewsitem(id = newsitem.id)
    when(mongoRepository.getResourceById(newsitem.id)).thenReturn(Future.successful(Some(newsitem)))
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(Future.successful(List(geotaggingVote)))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val geotaggedVotesOnModel = mv.getModel.get("geotag_votes").asInstanceOf[java.util.List[GeotaggingVote]]
    assertEquals(1, geotaggedVotesOnModel.size)
    assertEquals(geotaggingVote, geotaggedVotesOnModel.get(0))
  }

}
