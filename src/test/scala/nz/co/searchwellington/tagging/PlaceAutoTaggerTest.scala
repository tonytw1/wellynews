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

class PlaceAutoTaggerTest extends TestCase {
  private var placesTag: Tag = null
  private var aroValleyTag: Tag = null
  private var placeAutoTagger: PlaceAutoTagger = null
  private var aroValleyNewsitem: Newsitem = null
  private var tagDAO: TagDAO = mock(classOf[TagDAO])

  @throws(classOf[Exception])
  protected override def setUp {
    placesTag = new Tag(1, "places", "Places", null, new HashSet[Tag], 0, false, false)
    aroValleyTag = new Tag(2, "arovalley", "Aro Valley", placesTag, new HashSet[Tag], 0, false, false)
    placesTag.addChild(aroValleyTag)
    placeAutoTagger = new PlaceAutoTagger(tagDAO)
    when(tagDAO.loadTagByName("places")).thenReturn(placesTag)
  }

  @throws(classOf[Exception])
  def testShouldTagNewsitemWithPlaceTags {
    aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the Aro Valley... Test", null, null, null, null, null)
    val suggestedTags: Set[Tag] = placeAutoTagger.suggestTags(aroValleyNewsitem)
    assertTrue(suggestedTags.contains(aroValleyTag))
  }

  @throws(classOf[Exception])
  def testPlaceAutoTaggingShouldBeCaseInsensitive {
    aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the aro valley... Test", null, null, null, null, null)
    val suggestedTags: Set[Tag] = placeAutoTagger.suggestTags(aroValleyNewsitem)
    assertTrue(suggestedTags.contains(aroValleyTag))
  }

}