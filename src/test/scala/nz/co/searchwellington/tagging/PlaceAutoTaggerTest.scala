package nz.co.searchwellington.tagging

import org.junit.Assert._
import org.mockito.Mockito.mock
import org.mockito.Mockito.when
import java.util.HashSet
import junit.framework.TestCase
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.model.NewsitemImpl
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.TagDAO

import scala.collection.JavaConverters._

class PlaceAutoTaggerTest extends TestCase {

  private var placesTag: Tag = null
  private var aroValleyTag: Tag = null
  private var aroValleyNewsitem: Newsitem = null

  private var tagDAO: TagDAO = mock(classOf[TagDAO])

  private var placeAutoTagger: PlaceAutoTagger = null

  @throws(classOf[Exception])
  protected override def setUp {
    placesTag = new Tag().name("places").displayName("Places");
    aroValleyTag = new Tag().name("arovalley").displayName("Aro Valley").parent(placesTag);
    placesTag.addChild(aroValleyTag)

    when(tagDAO.loadTagByName("places")).thenReturn(placesTag)
    placeAutoTagger = new PlaceAutoTagger(tagDAO)
  }

  @throws(classOf[Exception])
  def testShouldTagNewsitemsWithPlaceTags {
    aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the Aro Valley... Test", null, null, null, null, null)

    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)

    assertTrue(suggestedTags.contains(aroValleyTag))
  }

  @throws(classOf[Exception])
  def testPlaceAutoTaggingShouldBeCaseInsensitive {
    aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the aro valley... Test", null, null, null, null, null)

    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)
    assertTrue(suggestedTags.contains(aroValleyTag))
  }

}