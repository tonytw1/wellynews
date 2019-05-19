package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model.{Resource, Tag, Tagging, User, Website}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.assertTrue
import org.junit.{Before, Test}
import org.mockito.Mockito
import org.mockito.Mockito.{mock, verify, when}

import scala.concurrent.Future

class HandTaggingServiceTest {
  private val handTaggingDAO = mock(classOf[HandTaggingDAO])
  private val frontendContentUpdater = mock(classOf[FrontendContentUpdater])
  private val mongoRepository = mock(classOf[MongoRepository])

  private val handTaggingService = new HandTaggingService(handTaggingDAO, frontendContentUpdater, mongoRepository)

  private val tag = mock(classOf[Tag])
  private val taggingUser = User()
  private val handTagging =  Tagging(taggingUser._id, tag._id)

  private val taggedResource = Website(resource_tags = Seq(handTagging))
  private val previousUser =  mock(classOf[User])
  private val newUser =  mock(classOf[User])

  @Before def setup {
    when(previousUser.getName).thenReturn("Previous User")
    when(newUser.getName).thenReturn("New User")
  }

  @Test
  def clearingTagVotesClearAllVotesForThatTagFromTheDatabase {
    when(mongoRepository.getResourceIdsByTag(tag)).thenReturn(Future.successful(Seq(taggedResource._id)))
    when(mongoRepository.getResourceByObjectId(taggedResource._id)).thenReturn(Future.successful(Some(taggedResource)))

    handTaggingService.clearTaggingsForTag(tag)

    verify(handTaggingDAO).deleteTagFromResource(tag, taggedResource)
  }

  @Test
  def clearingTagVotesShouldtriggerFrontendContentUpdateForTheEffectedResources {
    when(mongoRepository.getResourceIdsByTag(tag)).thenReturn(Future.successful(Seq(taggedResource._id)))
    when(mongoRepository.getResourceByObjectId(taggedResource._id)).thenReturn(Future.successful(Some(taggedResource)))

    handTaggingService.clearTaggingsForTag(tag)

    verify(frontendContentUpdater).update(taggedResource)
  }

  @Test
  def shouldReassignTheVotesUserAndPreformFrontendUpdateWhenTransferingVotes {
    when(mongoRepository.getResourceIdsByTag(tag)).thenReturn(Future.successful(Seq(taggedResource._id)))
    when(mongoRepository.getResourceByObjectId(taggedResource._id)).thenReturn(Future.successful(Some(taggedResource)))

    handTaggingService.transferVotes(previousUser, newUser)

    //verify(handTagging).setUser(newUser)
    verify(frontendContentUpdater).update(taggedResource)
  }

}