package nz.co.searchwellington.tagging

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Newsitem, Tag}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert._
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class PlaceAutoTaggerTest extends ReasonableWaits {

  private val placesTag = Tag(_id = BSONObjectID.generate, id = UUID.randomUUID().toString, name = "places", display_name = "Places")
  private val aroValleyTag = Tag(id = UUID.randomUUID().toString, name = "arovalley", display_name = "Aro Valley", parent = Some(placesTag._id))
  private val islandBayTag = Tag(id = UUID.randomUUID().toString, name = "islandbay", display_name = "Island Bay", parent = Some(placesTag._id))

  private val aroValleyNewsitem = Newsitem(title = Some("Test newsitem"), description = Some(".. Student flats in the Aro Valley... Test"))

  private val tagDAO = mock(classOf[TagDAO])
  private val mongoRepository = mock(classOf[MongoRepository])

  private val placeAutoTagger = new PlaceAutoTagger(mongoRepository, tagDAO)

  @Before
  def setUp {
    when(mongoRepository.getTagByUrlWords("places")).thenReturn(Future.successful(Some(placesTag)))
    when(tagDAO.loadTagsByParent(placesTag._id)).thenReturn(Future.successful(List(aroValleyTag, islandBayTag)))
  }

  @Test
  def testShouldTagNewsitemsWithPlaceTags {
    val suggestedTags = Await.result(placeAutoTagger.suggestTags(aroValleyNewsitem), TenSeconds)

    assertTrue(suggestedTags.contains(aroValleyTag))
  }

  @Test
  def testPlaceAutoTaggingShouldBeCaseInsensitive {
    val suggestedTags =  Await.result(placeAutoTagger.suggestTags(aroValleyNewsitem), TenSeconds)

    assertTrue(suggestedTags.contains(aroValleyTag))
  }

}
