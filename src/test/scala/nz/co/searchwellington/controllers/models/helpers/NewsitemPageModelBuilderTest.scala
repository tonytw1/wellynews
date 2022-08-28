package nz.co.searchwellington.controllers.models.helpers

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.taggingvotes.{GeotaggingVote, HandTagging}
import nz.co.searchwellington.model.{Newsitem, Tag, User}
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertNull, assertTrue}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

class NewsitemPageModelBuilderTest extends ReasonableWaits {

  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val taggingReturnsOfficerService = mock(classOf[TaggingReturnsOfficerService])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])

  private implicit val currentSpan: Span = Span.current()

  private val modelBuilder = new NewsitemPageModelBuilder(contentRetrievalService, taggingReturnsOfficerService,
    mongoRepository, frontendResourceMapper)

  @Test
  def shouldBeValidForSingleNewsitemPath(): Unit = {
    val newsitem = Newsitem()

    val validPath = "/newsitem/" + newsitem.id
    val request = new MockHttpServletRequest
    request.setRequestURI(validPath)

    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def shouldShowNewsitem(): Unit = {
    val newsitem = Newsitem()
    val validPath = "/newsitem/" + newsitem.id
    val request = new MockHttpServletRequest
    request.setRequestURI(validPath)

    val place = Geocode(address = Some("Somewhere"))
    val frontendNewsitem = FrontendNewsitem(id = newsitem.id, place = Some(place))

    when(mongoRepository.getResourceById(newsitem.id)).thenReturn(Future.successful(Some(newsitem)))
    when(frontendResourceMapper.createFrontendResourceFrom(newsitem, None)).thenReturn(Future.successful(frontendNewsitem))
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(Future.successful(Seq.empty))
    when(taggingReturnsOfficerService.getTaggingsVotesForResource(newsitem)).thenReturn(Future.successful(Seq.empty))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val item = mv.get("item")
    assertEquals(frontendNewsitem, item)
  }

  @Test
  def shouldShowTaggingsAppliedToThisNewsitem(): Unit = {
    val newsitem = Newsitem()
    val validPath = "/newsitem/" + newsitem.id
    val request = new MockHttpServletRequest
    request.setRequestURI(validPath)

    val place = Geocode(address = Some("Somewhere"))
    val frontendNewsitem = FrontendNewsitem(id = newsitem.id, place = Some(place))

    val handTagging = HandTagging(tag = Tag(id = "123"), taggingUser = User())
    val geotagVotesForNewsitem = Seq(GeotaggingVote(geocode = place, weight = 1, explanation = "Some tagging"))
    val taggingVotesForNewsitem = Seq(handTagging)

    when(mongoRepository.getResourceById(newsitem.id)).thenReturn(Future.successful(Some(newsitem)))
    when(frontendResourceMapper.createFrontendResourceFrom(newsitem, None)).thenReturn(Future.successful(frontendNewsitem))
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(Future.successful(geotagVotesForNewsitem))
    when(taggingReturnsOfficerService.getTaggingsVotesForResource(newsitem)).thenReturn(Future.successful(taggingVotesForNewsitem))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val handTaggingVotes = mv.get("hand_tagging_votes")
    assertEquals(Seq(handTagging).asJava, handTaggingVotes, "Expect to be able to see all hand tagging votes for this newsitem")

    val otherTaggingVotes = mv.get("other_tagging_votes")
    assertEquals(Seq.empty.asJava, otherTaggingVotes, "Expect to be see other tagging votes")

    val geoTagVotes = mv.get("geotag_votes")
    assertEquals(geotagVotesForNewsitem.asJava, geoTagVotes,"Expect to be see geotagging votes")
  }

  @Test
  def shouldShowNewsitemOnMapIfItIsGeotagged(): Unit = {
    val id = UUID.randomUUID()
    val geotaggedNewsitem = Newsitem(id = id.toString)

    val validPath = "/newsitem/" + id.toString
    val request = new MockHttpServletRequest
    request.setRequestURI(validPath)

    val place = Geocode(address = Some("Somewhere"))
    val geotaggedFrontendNewsitem = FrontendNewsitem(id = id.toString, place = Some(place))

    when(mongoRepository.getResourceById(id.toString)).thenReturn(Future.successful(Some(geotaggedNewsitem))) // TODO properly exercise mapped option branch
    when(frontendResourceMapper.createFrontendResourceFrom(geotaggedNewsitem, None)).thenReturn(Future.successful(geotaggedFrontendNewsitem))
    when(taggingReturnsOfficerService.getTaggingsVotesForResource(geotaggedNewsitem)).thenReturn(Future.successful(Seq.empty))

    val geotaggingVote = GeotaggingVote(place, "Publisher's location", 1)
    when(taggingReturnsOfficerService.getGeotagVotesForResource(geotaggedNewsitem)).thenReturn(Future.successful(Seq(geotaggingVote)))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val geotagged = mv.get("geocoded").asInstanceOf[java.util.List[FrontendNewsitem]]
    assertNotNull(geotagged)
    assertEquals(1, geotagged.size)
    assertEquals(geotaggedFrontendNewsitem, geotagged.get(0))
  }

  @Test
  def shouldNotPopulateGeotaggedItemsIfNewsitemIsNotGeotagged(): Unit = {
    val id = UUID.randomUUID()
    val newsitem = Newsitem(id = id.toString)
    val frontendNewsitem = FrontendNewsitem(id = id.toString)

    val validPath = "/newsitem/" + newsitem.id
    val request = new MockHttpServletRequest
    request.setRequestURI(validPath)

    when(mongoRepository.getResourceById(id.toString)).thenReturn(Future.successful(Some(newsitem))) // TODO properly exercise mapped option branch
    when(frontendResourceMapper.createFrontendResourceFrom(newsitem, None)).thenReturn(Future.successful(frontendNewsitem))
    when(taggingReturnsOfficerService.getTaggingsVotesForResource(newsitem)).thenReturn(Future.successful(Seq.empty))
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(Future.successful(Seq.empty))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    assertNull(mv.get("geocoded"))
  }

  @Test
  def shouldDisplayGeotaggingVotes(): Unit = {
    val newsitem = Newsitem()
    val validPath = "/newsitem/" + newsitem.id
    val request = new MockHttpServletRequest
    request.setRequestURI(validPath)

    val frontendNewsitem = FrontendNewsitem(id = newsitem.id)
    when(mongoRepository.getResourceById(newsitem.id)).thenReturn(Future.successful(Some(newsitem)))
    when(frontendResourceMapper.createFrontendResourceFrom(newsitem, None)).thenReturn(Future.successful(frontendNewsitem))
    when(taggingReturnsOfficerService.getTaggingsVotesForResource(newsitem)).thenReturn(Future.successful(Seq.empty))

    val place = Geocode(address = Some("Somewhere"))
    val geotaggingVote = GeotaggingVote(place, "Publisher's location", 1)
    when(taggingReturnsOfficerService.getGeotagVotesForResource(newsitem)).thenReturn(Future.successful(Seq(geotaggingVote)))

    val mv = Await.result(modelBuilder.populateContentModel(request), TenSeconds).get

    val geotaggedVotesOnModel = mv.get("geotag_votes").asInstanceOf[java.util.List[GeotaggingVote]]
    assertEquals(1, geotaggedVotesOnModel.size)
    assertEquals(geotaggingVote, geotaggedVotesOnModel.get(0))
  }

}
