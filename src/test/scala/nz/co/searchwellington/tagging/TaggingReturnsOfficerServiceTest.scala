package nz.co.searchwellington.tagging

import java.util

import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model.{Newsitem, Tag}
import nz.co.searchwellington.repositories.HandTaggingDAO
import org.junit.Assert._
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}

import scala.collection.JavaConversions._

class TaggingReturnsOfficerServiceTest {

  private val placesTag = Tag(name = "places", display_name = "Places")
  private val aroValleyTag = Tag(name = "arovalley", display_name = "Aro Valley", parent = placesTag._id)

  private var aroValleyNewsitem: Newsitem = null

  @Mock private var handTaggingDAO: HandTaggingDAO = null

  private var taggingReturnsOfficerService: TaggingReturnsOfficerService = null

  @Before
  def setUp {
    MockitoAnnotations.initMocks(this)
    taggingReturnsOfficerService = new TaggingReturnsOfficerService(handTaggingDAO, null) // TODO
  }

  @Test
  def compliedTagsShouldContainAtLeastOneCopyOfEachManuallyAppliedTag {
    aroValleyNewsitem = Newsitem(id = "1", title = Some("Test newsitem"), description = Some(".. Student flats in the Aro Valley... Test"))
    val handTags = Seq(new HandTagging(user = null, tag = aroValleyTag))
    when(handTaggingDAO.getHandTaggingsForResource(aroValleyNewsitem)).thenReturn(handTags)

    val taggings = taggingReturnsOfficerService.compileTaggingVotes(aroValleyNewsitem)

    assertTrue(taggings.get(0).tag.equals(aroValleyTag)); // TODO not a great assert
  }

  @Test
  def indexTagsShouldContainAtLeastOneCopyOfEachManuallyAppliedTag {
    aroValleyNewsitem = Newsitem(id = "1", title = Some("Test newsitem"), description = Some(".. Student flats in the Aro Valley... Test"))
    val handTags = Seq(new HandTagging(user = null, tag = aroValleyTag))
    when(handTaggingDAO.getHandTaggingsForResource(aroValleyNewsitem)).thenReturn(handTags)

    var indexTags: util.Set[Tag] = taggingReturnsOfficerService.getIndexTagsForResource(aroValleyNewsitem)

    assertTrue(indexTags.contains(aroValleyTag))
  }

  @Test
  def shouldIncludePublishersTagsInNewsitemsIndexTags = {
    fail()
  }

}
