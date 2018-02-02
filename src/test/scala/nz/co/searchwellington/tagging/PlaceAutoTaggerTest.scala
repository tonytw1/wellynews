package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.{Newsitem, NewsitemImpl, Tag, TagBuilder}
import nz.co.searchwellington.repositories.TagDAO
import org.junit.Assert._
import org.mockito.{MockitoAnnotations, Mock}
import org.mockito.Mockito.{mock, when}
import org.junit.Test
import org.junit.Before

class PlaceAutoTaggerTest {

  private var placesTag: Tag = null
  private var aroValleyTag: Tag = null
  private var aroValleyNewsitem: Newsitem = null

  @Mock private var tagDAO: TagDAO  = null

  private var placeAutoTagger: PlaceAutoTagger = null

  @Before def setUp {
    MockitoAnnotations.initMocks(this)
    placesTag = new TagBuilder().name("places").displayName("Places").build
    aroValleyTag = new TagBuilder().name("arovalley").displayName("Aro Valley").parent(placesTag).build
    //placesTag.addChild(aroValleyTag)
    when(tagDAO.loadTagByName("places")).thenReturn(Some(placesTag))
    placeAutoTagger = new PlaceAutoTagger(tagDAO)
  }

  @Test def testShouldTagNewsitemWithPlaceTags {
    aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the Aro Valley... Test")
    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)
    assertTrue(suggestedTags.contains(aroValleyTag))
  }

  @Test def testPlaceAutoTaggingShouldBeCaseInsensitive {
    aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the aro valley... Test")
    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)
    assertTrue(suggestedTags.contains(aroValleyTag))
  }

}
