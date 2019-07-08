package nz.co.searchwellington.repositories

import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{mock, verify, when, verifyZeroInteractions}

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
  private val newUser = User(name = Some("New user"))

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
  def userCanAddTagToTaggableResource: Unit = {
    val user = User()
    val tag = Tag()
    val resource = Newsitem()

    val updated = handTaggingService.addTag(user, tag, resource)

    assertTrue(updated.resource_tags.nonEmpty)
    assertEquals(Tagging(user_id = user._id, tag_id = tag._id), updated.resource_tags.head)
    verifyZeroInteractions(mongoRepository)
  }

  @Test
  def usersCannotApplyTheSameTagMultipleTimes: Unit = {
    val user = User()
    val tag = Tag()
    val existingTagging = Tagging(user_id = user._id, tag_id = tag._id)
    val resource = Newsitem(resource_tags = Seq(existingTagging))

    val updated = handTaggingService.addTag(user, tag, resource)

    assertEquals(resource.resource_tags, updated.resource_tags)
    verifyZeroInteractions(mongoRepository)
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
  def shouldReassignTheVotesUserAndPreformFrontendUpdateWhenTransferringVotes {
    when(mongoRepository.getResourceIdsByTaggingUser(taggingUser)).thenReturn(Future.successful(Seq(taggedResource._id)))
    when(mongoRepository.getResourceByObjectId(taggedResource._id)).thenReturn(Future.successful(Some(taggedResource)))

    val updated = ArgumentCaptor.forClass(classOf[Resource])
    handTaggingService.transferVotes(taggingUser, newUser)

    verify(mongoRepository).saveResource(updated.capture())
    assertEquals(taggedResource._id, updated.getValue._id)
    assertEquals(newUser._id, updated.getValue.resource_tags.head.user_id)
  }

}