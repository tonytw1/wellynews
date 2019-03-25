package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model.{Newsitem, Tag, User, Website}
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert._
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, when}
import org.mockito.{Mock, MockitoAnnotations}

import scala.collection.JavaConversions._

class TaggingReturnsOfficerServiceTest {

  private val placesTag = Tag(name = "places", display_name = "Places")
  private val aroValleyTag = Tag(name = "arovalley", display_name = "Aro Valley", parent = placesTag._id)
  private val educationTag = Tag(name = "education", display_name = "Education")

  private val taggingUser = User(name = Some("auser"))

  private val victoriaUniversity = Website(title = Some("Victoria University"))

  private val aroValleyNewsitem = Newsitem(title = Some("Test newsitem"),
    description = Some(".. Student flats in the Aro Valley... Test"),
    publisher = Some(victoriaUniversity._id)
  )

  @Mock private var handTaggingDAO: HandTaggingDAO = null

  private var taggingReturnsOfficerService: TaggingReturnsOfficerService = null
  private val mongoRepository = mock(classOf[MongoRepository])

  @Before
  def setUp {
    MockitoAnnotations.initMocks(this)
    taggingReturnsOfficerService = new TaggingReturnsOfficerService(handTaggingDAO, mongoRepository)
  }

  @Test
  def compliedTagsShouldContainAtLeastOneCopyOfEachManuallyAppliedTag {
    val handTags = Seq(new HandTagging(user = taggingUser, tag = aroValleyTag))
    when(handTaggingDAO.getHandTaggingsForResource(aroValleyNewsitem)).thenReturn(handTags)
    when(handTaggingDAO.getHandTaggingsForResourceId(victoriaUniversity._id)).thenReturn(Seq(new HandTagging(user = taggingUser, tag = educationTag)))

    val taggings = taggingReturnsOfficerService.compileTaggingVotes(aroValleyNewsitem)

    assertTrue(taggings.get(0).tag.equals(aroValleyTag)); // TODO not a great assert
  }

  @Test
  def indexTagsShouldContainAtLeastOneCopyOfEachManuallyAppliedTag {
    val handTags = Seq(new HandTagging(user = taggingUser, tag = aroValleyTag))
    when(handTaggingDAO.getHandTaggingsForResource(aroValleyNewsitem)).thenReturn(handTags)
    when(handTaggingDAO.getHandTaggingsForResourceId(victoriaUniversity._id)).thenReturn(Seq(new HandTagging(user = taggingUser, tag = educationTag)))

    var indexTags = taggingReturnsOfficerService.getIndexTagsForResource(aroValleyNewsitem)

    assertTrue(indexTags.contains(aroValleyTag))
  }

  @Test
  def shouldIncludePublishersTagsInNewsitemsIndexTags = {
    when(handTaggingDAO.getHandTaggingsForResource(aroValleyNewsitem)).thenReturn(Seq(new HandTagging(user = taggingUser, tag = aroValleyTag)))
    when(handTaggingDAO.getHandTaggingsForResourceId(victoriaUniversity._id)).thenReturn(Seq(new HandTagging(user = taggingUser, tag = educationTag)))

    var indexTags = taggingReturnsOfficerService.getIndexTagsForResource(aroValleyNewsitem)

    assertTrue(indexTags.contains(educationTag))
  }

}
