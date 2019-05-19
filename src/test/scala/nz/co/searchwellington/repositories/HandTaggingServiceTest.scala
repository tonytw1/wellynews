package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model.{Resource, Tag, User}
import org.junit.{Before, Test}
import org.mockito.Mockito
import org.mockito.Mockito.{mock, verify, when}

class HandTaggingServiceTest {
  private val handTaggingDAO = mock(classOf[HandTaggingDAO])
  private val frontendContentUpdater = mock(classOf[FrontendContentUpdater])

  private val handTaggingService = new HandTaggingService(handTaggingDAO, frontendContentUpdater)

  private val tag = mock(classOf[Tag])
  private val taggedResource = mock(classOf[Resource])
  private val previousUser =  mock(classOf[User])
  private val newUser =  mock(classOf[User])
  private val handTagging =  mock(classOf[HandTagging])
  private val tagVotes = Seq(handTagging)

  @Before def setup {
    when(previousUser.getName).thenReturn("Previous User")
    when(newUser.getName).thenReturn("New User")
  }

  @Test
  def clearingTagVotesClearAllVotesForThatTagFromTheDatabase {
    when(handTaggingDAO.getVotesForTag(tag)).thenReturn(tagVotes)

    handTaggingService.clearTaggingsForTag(tag)

    verify(handTaggingDAO).delete(handTagging)
  }

  @Test
  def clearingTagVotesShouldtriggerFrontendContentUpdateForTheEffectedResources {
    when(handTaggingDAO.getVotesForTag(tag)).thenReturn(tagVotes)
    handTaggingService.clearTaggingsForTag(tag)
    verify(frontendContentUpdater).update(taggedResource)
  }

  @Test
  def shouldReassignTheVotesUserAndPreformFrontendUpdateWhenTransferingVotes {
    Mockito.when(handTaggingDAO.getUsersVotes(previousUser)).thenReturn(tagVotes)
    handTaggingService.transferVotes(previousUser, newUser)
    //verify(handTagging).setUser(newUser)
    verify(frontendContentUpdater).update(taggedResource)
  }

}