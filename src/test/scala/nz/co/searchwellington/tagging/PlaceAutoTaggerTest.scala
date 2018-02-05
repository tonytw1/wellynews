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
  private var islandBayTag: Tag = null
  private var aroValleyNewsitem: Newsitem = null

  @Mock private var tagDAO: TagDAO  = null

  private var placeAutoTagger: PlaceAutoTagger = null

  @Before def setUp {
    MockitoAnnotations.initMocks(this)
    placesTag = Tag(id = 123, name = "places", display_name = "Places")
    aroValleyTag = Tag(name = "arovalley", display_name = "Aro Valley", parent = Some(placesTag.id))
    islandBayTag = Tag(name = "islandbay", display_name = "Island Bay", parent = Some(placesTag.id))

    when(tagDAO.loadTagByName("places")).thenReturn(Some(placesTag))
    when(tagDAO.loadTagsByParent(123)).thenReturn(Seq(aroValleyTag, islandBayTag))

    placeAutoTagger = new PlaceAutoTagger(tagDAO)
  }

  @Test def testShouldTagNewsitemWithPlaceTags {
    aroValleyNewsitem = new NewsitemImpl(name = "Test newsitem", description = ".. Student flats in the Aro Valley... Test")

    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)

    assertTrue(suggestedTags.contains(aroValleyTag))
  }

  @Test def testPlaceAutoTaggingShouldBeCaseInsensitive {
    aroValleyNewsitem = new NewsitemImpl(name = "Test newsitem", description =  ".. Student flats in the aro valley... Test")

    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)

    assertTrue(suggestedTags.contains(aroValleyTag))
  }

}
