package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.{mock, when}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class TaggingReturnsOfficerServiceTest extends ReasonableWaits {

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

  private val taggingReturnsOfficerService: TaggingReturnsOfficerService = new TaggingReturnsOfficerService(handTaggingDAO, mongoRepository)

  @Test
  def compliedTagsShouldContainAtLeastOneCopyOfEachManuallyAppliedTag(): Unit = {
    val handTags = Seq(new HandTagging(user = taggingUser, tag = aroValleyTag))
    when(handTaggingDAO.getHandTaggingsForResource(aroValleyNewsitem)).thenReturn(Future.successful(handTags))
    when(handTaggingDAO.getHandTaggingsForResourceId(victoriaUniversity._id)).thenReturn(Future.successful(Seq(new HandTagging(user = taggingUser, tag = educationTag))))
    when(mongoRepository.getTagByObjectId(placesTag._id)).thenReturn(Future.successful(Some(placesTag)))

    val taggings = Await.result(taggingReturnsOfficerService.compileTaggingVotes(aroValleyNewsitem), TenSeconds)

    assertTrue(taggings.head.tag.equals(aroValleyTag)); // TODO not a great assert
  }

  @Test
  def indexTagsShouldContainAtLeastOneCopyOfEachManuallyAppliedTag(): Unit = {
    val handTags = Seq(new HandTagging(user = taggingUser, tag = aroValleyTag))
    when(handTaggingDAO.getHandTaggingsForResource(aroValleyNewsitem)).thenReturn(Future.successful(handTags))
    when(handTaggingDAO.getHandTaggingsForResourceId(victoriaUniversity._id)).thenReturn(Future.successful(Seq(new HandTagging(user = taggingUser, tag = educationTag))))
    when(mongoRepository.getTagByObjectId(placesTag._id)).thenReturn(Future.successful(Some(placesTag)))

    val indexTags = Await.result(taggingReturnsOfficerService.getIndexTagsForResource(aroValleyNewsitem), TenSeconds)

    assertTrue(indexTags.contains(aroValleyTag))
  }

  @Test
  def shouldIncludePublishersTagsInNewsitemsIndexTags() = {
    when(handTaggingDAO.getHandTaggingsForResource(aroValleyNewsitem)).thenReturn(Future.successful(Seq(HandTagging(user = taggingUser, tag = aroValleyTag))))
    when(handTaggingDAO.getHandTaggingsForResourceId(victoriaUniversity._id)).thenReturn(Future.successful(Seq(HandTagging(user = taggingUser, tag = educationTag))))
    when(mongoRepository.getTagByObjectId(placesTag._id)).thenReturn(Future.successful(Some(placesTag)))

    val indexTags = Await.result(taggingReturnsOfficerService.getIndexTagsForResource(aroValleyNewsitem), TenSeconds)

    assertTrue(indexTags.contains(educationTag))
  }

  @Test
  def shouldIncludeAncestorsOfPublishersTags(): Unit = {
    val cricketWellingtonNewsitem = Newsitem(title = Some("Cricket"),
      description = Some("Cricket thing"),
      publisher = Some(cricketWellington._id)
    )

    when(mongoRepository.getTagByObjectId(sportTag._id)).thenReturn(Future.successful(Some(sportTag)))
    when(handTaggingDAO.getHandTaggingsForResource(cricketWellingtonNewsitem)).thenReturn(Future.successful(Seq.empty))
    when(handTaggingDAO.getHandTaggingsForResourceId(cricketWellington._id)).thenReturn(Future.successful(Seq(HandTagging(user = taggingUser, tag = cricketTag))))

    val indexTags = Await.result(taggingReturnsOfficerService.getIndexTagsForResource(cricketWellingtonNewsitem), TenSeconds)

    assertTrue(indexTags.contains(sportTag))
  }

  @Test
  def shouldIncludeFeedsTagsInNewsitemIndexTags(): Unit = {
    val publicInputFeed = Feed(title = Some("Wellington City Council - Public Input"))

    val publicInputNewsitem = Newsitem(
      title = Some("Proposal to Discharge Encumbrance - 79 Dixon Street, Te Aro"),
      feed = Some(publicInputFeed._id)
    )

    when(handTaggingDAO.getHandTaggingsForResourceId(publicInputFeed._id)).thenReturn(Future.successful(Seq(HandTagging(user = taggingUser, tag = consultationTag))))
    when(handTaggingDAO.getHandTaggingsForResource(publicInputNewsitem)).thenReturn(Future.successful(Seq.empty))

    val indexTags = Await.result(taggingReturnsOfficerService.getIndexTagsForResource(publicInputNewsitem), TenSeconds)

    import scala.collection.JavaConverters._
    com.google.common.truth.Truth.assertThat(indexTags.asJava).contains(consultationTag)
  }

  @Test
  def shouldIncludeAncestorsOfFeedTags(): Unit = {
    val cricketWellingtonNewsFeed = Feed(title = Some("Cricket Wellington news"))

    val cricketWellingtonNewsitem = Newsitem(title = Some("Cricket"),
      description = Some("Cricket thing"),
      feed = Some(cricketWellingtonNewsFeed._id)
    )

    when(mongoRepository.getTagByObjectId(sportTag._id)).thenReturn(Future.successful(Some(sportTag)))
    when(handTaggingDAO.getHandTaggingsForResource(cricketWellingtonNewsitem)).thenReturn(Future.successful(Seq.empty))
    when(handTaggingDAO.getHandTaggingsForResourceId(cricketWellingtonNewsFeed._id)).thenReturn(Future.successful(Seq(HandTagging(user = taggingUser, tag = cricketTag))))

    val indexTags = Await.result(taggingReturnsOfficerService.getIndexTagsForResource(cricketWellingtonNewsitem), TenSeconds)

    assertTrue(indexTags.contains(sportTag))
  }

}
