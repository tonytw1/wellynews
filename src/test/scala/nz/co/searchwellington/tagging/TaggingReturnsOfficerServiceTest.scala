package nz.co.searchwellington.tagging

import nz.co.searchwellington.model._
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert._
import org.junit.Test
import org.mockito.Mockito.{mock, when}

import scala.collection.JavaConversions._
import scala.concurrent.Future

class TaggingReturnsOfficerServiceTest {

  private val placesTag = Tag(name = "places", display_name = "Places")
  private val aroValleyTag = Tag(name = "arovalley", display_name = "Aro Valley", parent = Some(placesTag._id))
  private val educationTag = Tag(name = "education", display_name = "Education")
  private val consultationTag = Tag(name = "consultation", display_name = "Consultation")
  private val sportTag = Tag(name = "sport", display_name = "Sport")
  private val cricketTag = Tag(name = "cricket", display_name = "Cricket", parent = Some(sportTag._id))

  private val taggingUser = User(name = Some("auser"))

  private val victoriaUniversity = Website(title = Some("Victoria University"))
  private val cricketWellington = Website(title = Some("Cricket Wellington"))

  private val aroValleyNewsitem = Newsitem(title = Some("Test newsitem"),
    description = Some(".. Student flats in the Aro Valley... Test"),
    publisher = Some(victoriaUniversity._id)
  )

  private val handTaggingDAO = mock(classOf[HandTaggingDAO])
  private val mongoRepository = mock(classOf[MongoRepository])

  private val taggingReturnsOfficerService: TaggingReturnsOfficerService =  new TaggingReturnsOfficerService(handTaggingDAO, mongoRepository)

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

    val indexTags = taggingReturnsOfficerService.getIndexTagsForResource(aroValleyNewsitem)

    assertTrue(indexTags.contains(educationTag))
  }

  @Test
  def shouldIncludeAncestorsOfPublishersTags = {
    val cricketWellingtonNewsitem = Newsitem(title = Some("Cricket"),
      description = Some("Cricket thing"),
      publisher = Some(cricketWellington._id)
    )

    when(mongoRepository.getTagByObjectId(sportTag._id)).thenReturn(Future.successful(Some(sportTag)))
    when(handTaggingDAO.getHandTaggingsForResource(cricketWellingtonNewsitem)).thenReturn(Seq.empty)
    when(handTaggingDAO.getHandTaggingsForResourceId(cricketWellington._id)).thenReturn(Seq(new HandTagging(user = taggingUser, tag = cricketTag)))

    val indexTags = taggingReturnsOfficerService.getIndexTagsForResource(cricketWellingtonNewsitem)

    assertTrue(indexTags.contains(sportTag))
  }

  @Test
  def shouldIncludeFeedsTagsInNewsitemIndexTags = {
    val publicInputFeed = Feed(title = Some("Wellington City Council - Public Input"))

    val publicInputNewsitem = Newsitem(
      title = Some("Proposal to Discharge Encumbrance - 79 Dixon Street, Te Aro"),
      feed = Some(publicInputFeed._id)
    )

    when(handTaggingDAO.getHandTaggingsForResourceId(publicInputFeed._id)).thenReturn(Seq(new HandTagging(user =taggingUser, tag = consultationTag)))
    when(handTaggingDAO.getHandTaggingsForResource(publicInputNewsitem)).thenReturn(Seq.empty)

    var indexTags = taggingReturnsOfficerService.getIndexTagsForResource(publicInputNewsitem)

    assertTrue(indexTags.contains(consultationTag))
  }

}
