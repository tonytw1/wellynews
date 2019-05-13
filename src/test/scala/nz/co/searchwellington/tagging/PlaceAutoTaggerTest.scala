package nz.co.searchwellington.tagging

import java.util.UUID

import nz.co.searchwellington.model.{Newsitem, Tag}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert._
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

class PlaceAutoTaggerTest {

  private val placesTag = Tag(_id = BSONObjectID.generate, id = UUID.randomUUID().toString, name = "places", display_name = "Places")
  private val aroValleyTag = Tag(id = UUID.randomUUID().toString, name = "arovalley", display_name = "Aro Valley", parent = Some(placesTag._id))
  private val islandBayTag = Tag(id = UUID.randomUUID().toString, name = "islandbay", display_name = "Island Bay", parent = Some(placesTag._id))

  private val aroValleyNewsitem = Newsitem(title = Some("Test newsitem"), description = Some(".. Student flats in the Aro Valley... Test"))

  private val tagDAO = mock(classOf[TagDAO])
  private val mongoRepository = mock(classOf[MongoRepository])

  private val placeAutoTagger: PlaceAutoTagger = new PlaceAutoTagger(mongoRepository, tagDAO)

  @Before
  def setUp {
    when(mongoRepository.getTagByUrlWords("places")).thenReturn(Future.successful(Some(placesTag)))
    when(tagDAO.loadTagsByParent(placesTag._id)).thenReturn(Seq(aroValleyTag, islandBayTag))
  }

  @Test
  def testShouldTagNewsitemWithPlaceTags {
    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)

    assertTrue(suggestedTags.contains(aroValleyTag))
  }

  @Test
  def testPlaceAutoTaggingShouldBeCaseInsensitive {
    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)

    assertTrue(suggestedTags.contains(aroValleyTag))
  }

}
