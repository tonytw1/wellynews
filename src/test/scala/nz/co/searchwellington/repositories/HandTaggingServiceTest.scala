package nz.co.searchwellington.repositories

import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.taggingvotes.HandTagging
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class HandTaggingServiceTest {
  @Mock private[repositories] val handTaggingDAO: HandTaggingDAO = null
  @Mock private[repositories] val frontendContentUpdater: FrontendContentUpdater = null
  @Mock private[repositories] val tag: Tag = null
  @Mock private[repositories] val taggedResource: Resource = null
  @Mock private[repositories] val previousUser: User = null
  @Mock private[repositories] val newUser: User = null
  @Mock private[repositories] val handTagging: HandTagging = null
  private var tagVotes: Seq[HandTagging] = null
  private var handTaggingService: HandTaggingService = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    tagVotes = Seq(handTagging)
    when(previousUser.getName).thenReturn("Previous User")
    when(newUser.getName).thenReturn("New User")
    handTaggingService = new HandTaggingService(handTaggingDAO, frontendContentUpdater)
  }

  @Test
  @throws[Exception]
  def clearingTagVotesClearAllVotesForThatTagFromTheDatabase {
    when(handTaggingDAO.getVotesForTag(tag)).thenReturn(tagVotes)
    handTaggingService.clearTaggingsForTag(tag)
    verify(handTaggingDAO).delete(handTagging)
  }

  @Test
  @throws[Exception]
  def clearingTagVotesShouldtriggerFrontendContentUpdateForTheEffectedResources {
    when(handTaggingDAO.getVotesForTag(tag)).thenReturn(tagVotes)
    handTaggingService.clearTaggingsForTag(tag)
    verify(frontendContentUpdater).update(taggedResource)
  }

  @Test
  @throws[Exception]
  def shouldReassignTheVotesUserAndPreformFrontendUpdateWhenTransferingVotes {
    Mockito.when(handTaggingDAO.getUsersVotes(previousUser)).thenReturn(tagVotes)
    handTaggingService.transferVotes(previousUser, newUser)
    //verify(handTagging).setUser(newUser)
    verify(frontendContentUpdater).update(taggedResource)
  }

}