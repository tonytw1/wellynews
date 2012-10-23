package nz.co.searchwellington.repositories;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.taggingvotes.HandTagging;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class HandTaggingServiceTest {

	@Mock HandTaggingDAO handTaggingDAO;
	@Mock FrontendContentUpdater frontendContentUpdater;
	
	@Mock Tag tag;
	@Mock Resource taggedResource;
	@Mock User previousUser;
	@Mock User newUser;
	@Mock HandTagging handTagging;
	
	private List<HandTagging> tagVotes;
	
	private HandTaggingService handTaggingService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		tagVotes = Lists.newArrayList();	
		when(handTagging.getResource()).thenReturn(taggedResource);
		tagVotes.add(handTagging);
		when(previousUser.getName()).thenReturn("Previous User");
		when(newUser.getName()).thenReturn("New User");

		handTaggingService = new HandTaggingService(handTaggingDAO, frontendContentUpdater);		
	}
	
	@Test
	public void clearingTagVotesClearAllVotesForThatTagFromTheDatabase() throws Exception {				
		Mockito.when(handTaggingDAO.getVotesForTag(tag)).thenReturn(tagVotes);
		handTaggingService.clearTaggingsForTag(tag);
		verify(handTaggingDAO).delete(handTagging);
	}
	
	@Test
	public void clearingTagVotesShouldtriggerFrontendContentUpdateForTheEffectedResources() throws Exception {
		when(handTaggingDAO.getVotesForTag(tag)).thenReturn(tagVotes);
		handTaggingService.clearTaggingsForTag(tag);	
		verify(frontendContentUpdater).update(taggedResource);		
	}
	
	@Test
	public void shouldReassignTheVotesUserAndPreformFrontendUpdateWhenTransferingVotes() throws Exception {
		Mockito.when(handTaggingDAO.getUsersVotes(previousUser)).thenReturn(tagVotes);
		handTaggingService.transferVotes(previousUser, newUser);
		verify(handTagging).setUser(newUser);
		verify(frontendContentUpdater).update(taggedResource);
	}
}
