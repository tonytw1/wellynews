package nz.co.searchwellington.tagging

import com.google.common.truth.Truth._
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.geo.{Geocode, LatLong}
import nz.co.searchwellington.model.taggingvotes.{GeotaggingVote, HandTagging}
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class TaggingReturnsOfficerServiceTest extends ReasonableWaits {

  private val aroValley = Geocode(address = Some("Aro Valley"), latLong = Some(LatLong(-41.2954, 174.7662)))
  private val placesTag = Tag(name = "places", display_name = "Places")
  private val aroValleyTag = Tag(name = "arovalley", display_name = "Aro Valley", parent = Some(placesTag._id), geocode = Some(aroValley))
  private val educationTag = Tag(name = "education", display_name = "Education")

  private val taggingUser = User(name = Some("auser"))

  private val victoriaUniversity = Website(title = "Victoria University")

  private val aroValleyNewsitem = Newsitem(title = "Test newsitem",
    description = Some(".. Student flats in the Aro Valley... Test"),
    publisher = Some(victoriaUniversity._id)
  )

  private val handTaggingDAO = mock(classOf[HandTaggingDAO])
  private val mongoRepository = mock(classOf[MongoRepository])

  private val taggingReturnsOfficerService = new TaggingReturnsOfficerService(handTaggingDAO, mongoRepository)

  @Test
  def compliedTagsShouldContainAtLeastOneCopyOfEachManuallyAppliedTag(): Unit = {
    val handTags = Seq(HandTagging(taggingUser = taggingUser, tag = aroValleyTag))
    when(handTaggingDAO.getHandTaggingsForResource(aroValleyNewsitem)).thenReturn(Future.successful(handTags))
    when(mongoRepository.getResourceByObjectId(victoriaUniversity._id)).thenReturn(Future.successful(Some(victoriaUniversity)))
    when(handTaggingDAO.getHandTaggingsForResource(victoriaUniversity)).thenReturn(Future.successful(Seq(HandTagging(taggingUser = taggingUser, tag = educationTag))))
    when(mongoRepository.getTagByObjectId(placesTag._id)).thenReturn(Future.successful(Some(placesTag)))

    val votes = Await.result(taggingReturnsOfficerService.getTaggingsVotesForResource(aroValleyNewsitem), TenSeconds)

    assertThat(votes.head.tag).isEqualTo(aroValleyTag) // TODO not a great assert
  }

  @Test
  def geotaggingVotesForNewsitemShouldContainHandTaggingTagsGeocodes(): Unit = {
    when(mongoRepository.getTagByObjectId(placesTag._id)).thenReturn(Future.successful(Some(placesTag)))
    val newsitemWithNoPublisher = aroValleyNewsitem.copy(publisher = None)
    val handTags = Seq(HandTagging(taggingUser = taggingUser, tag = aroValleyTag))
    when(handTaggingDAO.getHandTaggingsForResource(newsitemWithNoPublisher)).thenReturn(Future.successful(handTags))

    val geotaggingVotes = Await.result(taggingReturnsOfficerService.getGeotagVotesForResource(newsitemWithNoPublisher), TenSeconds)

    assertTrue(geotaggingVotes.contains(GeotaggingVote(geocode = aroValley, explanation = "Aro Valley tag geocode", 1)))
  }

}
