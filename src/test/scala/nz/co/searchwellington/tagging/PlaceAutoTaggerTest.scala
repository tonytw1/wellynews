package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.{Newsitem, NewsitemImpl, Tag, TagBuilder}
import nz.co.searchwellington.repositories.TagDAO
import org.junit.Assert._
import org.mockito.Mockito.{mock, when}
import org.junit.Test
import org.junit.Before

class PlaceAutoTaggerTest {

  private var placesTag: Tag = null
  private var aroValleyTag: Tag = null
  private var aroValleyNewsitem: Newsitem = null

  private var tagDAO: TagDAO = mock(classOf[TagDAO])

  private var placeAutoTagger: PlaceAutoTagger = null

  @throws(classOf[Exception])
  @Before def setUp {
    placesTag = new TagBuilder().name("places").displayName("Places").build
    aroValleyTag = new TagBuilder().name("arovalley").displayName("Aro Valley").parent(placesTag).build
    placesTag.addChild(aroValleyTag)
    when(tagDAO.loadTagByName("places")).thenReturn(placesTag)
    placeAutoTagger = new PlaceAutoTagger(tagDAO)
  }

  @throws(classOf[Exception])
  @Test def testShouldTagNewsitemWithPlaceTags {
    aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the Aro Valley... Test", null, null, null, null, null)
    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)
    assertTrue(suggestedTags.contains(aroValleyTag))
  }

  @throws(classOf[Exception])
  @Test def testPlaceAutoTaggingShouldBeCaseInsensitive {
    aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the aro valley... Test", null, null, null, null, null)
    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)
    assertTrue(suggestedTags.contains(aroValleyTag))
  }

}