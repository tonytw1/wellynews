package nz.co.searchwellington.tagging

import java.util.UUID

import nz.co.searchwellington.model.{Newsitem, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.junit.Assert._
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}

class PlaceAutoTaggerTest {

  private var placesTag: Tag = null
  private var aroValleyTag: Tag = null
  private var islandBayTag: Tag = null
  private var aroValleyNewsitem: Newsitem = null

  @Mock private var tagDAO: TagDAO  = null

  private var placeAutoTagger: PlaceAutoTagger = null

  @Before def setUp {
    MockitoAnnotations.initMocks(this)
    placesTag = Tag(id = UUID.randomUUID().toString, name = "places", display_name = "Places")
    aroValleyTag = Tag(id = UUID.randomUUID().toString, name = "arovalley", display_name = "Aro Valley", parent = None) // TODO places as parent
    islandBayTag = Tag(id = UUID.randomUUID().toString, name = "islandbay", display_name = "Island Bay", parent = None)

    when(tagDAO.loadTagByName("places")).thenReturn(Some(placesTag))
    when(tagDAO.loadTagsByParent(UUID.randomUUID().toString)).thenReturn(Seq(aroValleyTag, islandBayTag)) // TOOD parent id

    placeAutoTagger = new PlaceAutoTagger(tagDAO)
  }

  @Test def testShouldTagNewsitemWithPlaceTags {
    aroValleyNewsitem = Newsitem(title = Some("Test newsitem"), description = Some(".. Student flats in the Aro Valley... Test"))

    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)

    assertTrue(suggestedTags.contains(aroValleyTag))
  }

  @Test def testPlaceAutoTaggingShouldBeCaseInsensitive {
    aroValleyNewsitem = Newsitem(title = Some("Test newsitem"), description = Some(".. Student flats in the aro valley... Test"))

    val suggestedTags = placeAutoTagger.suggestTags(aroValleyNewsitem)

    assertTrue(suggestedTags.contains(aroValleyTag))
  }

}
