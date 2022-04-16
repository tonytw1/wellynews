package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Newsitem, Tag}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert._
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import reactivemongo.api.bson.BSONObjectID

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class PlaceAutoTaggerTest extends ReasonableWaits {

  private val placesTag = Tag(_id = BSONObjectID.generate, id = UUID.randomUUID().toString, name = "places", display_name = "Places")
  private val aroValleyTag = Tag(id = UUID.randomUUID().toString, name = "arovalley", display_name = "Aro Valley", parent = Some(placesTag._id))
  private val islandBayTag = Tag(id = UUID.randomUUID().toString, name = "islandbay", display_name = "Island Bay", parent = Some(placesTag._id))

  private val aroValleyNewsitem = Newsitem(title = "Test newsitem", description = Some(".. Student flats in the Aro Valley... Test"))

  private val mongoRepository = mock(classOf[MongoRepository])

  private val placeAutoTagger = new PlaceAutoTagger(mongoRepository)

  @Before
  def setUp(): Unit = {
    when(mongoRepository.getTagByUrlWords("places")).thenReturn(Future.successful(Some(placesTag)))
    when(mongoRepository.getTagsByParent(placesTag._id)).thenReturn(Future.successful(List(aroValleyTag, islandBayTag)))
    when(mongoRepository.getTagsByParent(aroValleyTag._id)).thenReturn(Future.successful(List.empty))
    when(mongoRepository.getTagsByParent(islandBayTag._id)).thenReturn(Future.successful(List.empty))
  }

  @Test
  def testShouldTagNewsitemsWithPlaceTags(): Unit = {
    val suggestedTags = Await.result(placeAutoTagger.suggestTags(aroValleyNewsitem), TenSeconds)

    assertTrue(suggestedTags.contains(aroValleyTag))
  }

  @Test
  def testPlaceAutoTaggingShouldBeCaseInsensitive(): Unit = {
    val suggestedTags =  Await.result(placeAutoTagger.suggestTags(aroValleyNewsitem), TenSeconds)

    assertTrue(suggestedTags.contains(aroValleyTag))
  }

}
