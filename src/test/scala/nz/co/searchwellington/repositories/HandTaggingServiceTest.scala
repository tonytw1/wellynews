package nz.co.searchwellington.repositories

import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, Test}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{mock, verify, when}

import scala.concurrent.Future

class HandTaggingServiceTest {
  private val handTaggingDAO = mock(classOf[HandTaggingDAO])
  private val frontendContentUpdater = mock(classOf[FrontendContentUpdater])
  private val mongoRepository = mock(classOf[MongoRepository])

  private val handTaggingService = new HandTaggingService(handTaggingDAO, frontendContentUpdater, mongoRepository)

  private val tag = Tag()
  private val taggingUser = User()
  private val handTagging = Tagging(tag_id = tag._id, user_id = taggingUser._id)

  private val taggedResource = Website(resource_tags = Seq(handTagging))
  private val previousUser = mock(classOf[User])
  private val newUser = mock(classOf[User])

  @Before def setup {
    when(previousUser.getName).thenReturn("Previous User")
    when(newUser.getName).thenReturn("New User")
  }

  @Test
  def clearingTagVotesClearAllVotesForThatTagFromTheDatabase {
    when(mongoRepository.getResourceIdsByTag(tag)).thenReturn(Future.successful(Seq(taggedResource._id)))
    when(mongoRepository.getResourceByObjectId(taggedResource._id)).thenReturn(Future.successful(Some(taggedResource)))

    val updated = ArgumentCaptor.forClass(classOf[Resource])

    handTaggingService.clearTaggingsForTag(tag)

    verify(mongoRepository).saveResource(updated.capture())
    assertEquals(taggedResource._id, updated.getValue._id)
    assertTrue(updated.getValue.resource_tags.isEmpty)
  }

  @Test
  def clearingTagVotesShouldtriggerFrontendContentUpdateForTheEffectedResources {
    when(mongoRepository.getResourceIdsByTag(tag)).thenReturn(Future.successful(Seq(taggedResource._id)))
    when(mongoRepository.getResourceByObjectId(taggedResource._id)).thenReturn(Future.successful(Some(taggedResource)))

    val updated = ArgumentCaptor.forClass(classOf[Resource])

    handTaggingService.clearTaggingsForTag(tag)

    verify(frontendContentUpdater).update(updated.capture())
    assertEquals(taggedResource._id, updated.getValue._id)
    assertTrue(updated.getValue.resource_tags.isEmpty)
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