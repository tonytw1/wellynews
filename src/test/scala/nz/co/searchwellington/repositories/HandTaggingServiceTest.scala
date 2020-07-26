package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.{Tagging, _}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, verify, verifyZeroInteractions, when}
import org.mockito.{ArgumentCaptor, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class HandTaggingServiceTest {
  private val frontendContentUpdater = mock(classOf[FrontendContentUpdater])
  private val mongoRepository = mock(classOf[MongoRepository])

  private val handTaggingService = new HandTaggingService(frontendContentUpdater, mongoRepository)

  private val tag = Tag()
  private val taggingUser = User()
  private val handTagging = Tagging(tag_id = tag._id, user_id = taggingUser._id)

  private val taggedResource = Website(resource_tags = Seq(handTagging))
  private val newUser = User(name = Some("New user"))

  implicit val ec = ExecutionContext.Implicits.global

  @Test
  def clearingTagVotesClearAllVotesForThatTagFromTheDatabase {
    when(mongoRepository.getResourceIdsByTag(tag)).thenReturn(Future.successful(Seq(taggedResource._id)))
    when(mongoRepository.getResourceByObjectId(taggedResource._id)).thenReturn(Future.successful(Some(taggedResource)))

    val updated = ArgumentCaptor.forClass(classOf[Resource])
    handTaggingService.clearTaggingsForTag(tag)

    // Expect the previously tagged resource to be updated with the tagging for the deleted tag removed
    verify(mongoRepository).saveResource(updated.capture())(Matchers.eq(ec))
    assertEquals(taggedResource._id, updated.getValue._id)
    assertTrue(updated.getValue.resource_tags.isEmpty)
  }

  @Test
  def userCanAddTagToTaggableResource: Unit = {
    val user = User()
    val tag = Tag()
    val resource = Newsitem()

    val updated = handTaggingService.addUserTagging(user, tag, resource)

    assertTrue(updated.resource_tags.nonEmpty)
    assertEquals(Tagging(user_id = user._id, tag_id = tag._id), updated.resource_tags.head)
    verifyZeroInteractions(mongoRepository)
  }

  @Test
  def canSetUsersTagsForResource: Unit = {
    val user = User()
    val anotherUser = User()

    val tag = Tag(name = "Tag")
    val anotherTag = Tag(name = "Another tag")
    val yetAnotherTag = Tag(name = "Yet another tag")

    val usersExistingTaggings: Seq[Tagging] = Seq(Tagging(user_id = user._id, tag_id = tag._id))
    val anotherUserApplyingTag = Tagging(user_id = anotherUser._id, tag_id = tag._id)
    val anotherUsersTaggings = Seq(anotherUserApplyingTag)

    val newsitem = Newsitem(resource_tags = usersExistingTaggings ++ anotherUsersTaggings)
    val usersUpdatedTaggings = Seq(anotherTag, yetAnotherTag).map(_._id).toSet

    val updated = handTaggingService.setUsersTagging(user, usersUpdatedTaggings, newsitem)

    assertTrue("New tag should have been applied", updated.resource_tags.contains(Tagging(user_id = user._id, tag_id = yetAnotherTag._id)))
    assertFalse("Old tag should have been removed", updated.resource_tags.contains(Tagging(user_id = user._id, tag_id = tag._id)))
    assertTrue("A user updating their tags for a resource should not effect other user's tagging of the same resource",
      updated.resource_tags.contains(anotherUserApplyingTag))
  }

  @Test
  def usersCannotApplyTheSameTagMultipleTimes: Unit = {
    val user = User()
    val tag = Tag()
    val existingTagging = Tagging(user_id = user._id, tag_id = tag._id)
    val resource = Newsitem(resource_tags = Seq(existingTagging))

    val updated = handTaggingService.addUserTagging(user, tag, resource)

    assertEquals(resource.resource_tags, updated.resource_tags)
    verifyZeroInteractions(mongoRepository)
  }

  @Test
  def clearingTagVotesShouldtriggerFrontendContentUpdateForTheEffectedResources {
    when(mongoRepository.getResourceIdsByTag(tag)).thenReturn(Future.successful(Seq(taggedResource._id)))
    when(mongoRepository.getResourceByObjectId(taggedResource._id)).thenReturn(Future.successful(Some(taggedResource)))

    handTaggingService.clearTaggingsForTag(tag)

    verify(frontendContentUpdater).update(taggedResource.copy(resource_tags = Seq.empty))
  }

  @Test
  def shouldReassignTheVotesUserAndPreformFrontendUpdateWhenTransferringVotes {
    when(mongoRepository.getResourceIdsByTaggingUser(taggingUser)).thenReturn(Future.successful(Seq(taggedResource._id)))
    when(mongoRepository.getResourceByObjectId(taggedResource._id)).thenReturn(Future.successful(Some(taggedResource)))

    val updated = ArgumentCaptor.forClass(classOf[Resource])
    handTaggingService.transferVotes(taggingUser, newUser)

    verify(mongoRepository).saveResource(updated.capture())(Matchers.eq(ec))
    assertEquals(taggedResource._id, updated.getValue._id)
    assertEquals(newUser._id, updated.getValue.resource_tags.head.user_id)
  }

}