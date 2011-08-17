package nz.co.searchwellington.repositories;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.HandTagging;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class HandTaggingServiceTest {

	@Mock HandTaggingDAO handTaggingDAO;
	@Mock FrontendContentUpdater frontendContentUpdater;
	
	@Mock Tag tag;
	@Mock Resource taggedResource;
	
	private List<HandTagging> tagVotes;
	private HandTagging handTagging;
	
	private HandTaggingService handTaggingService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		tagVotes = new ArrayList<HandTagging>();
		handTagging = new HandTagging();
		handTagging.setResource(taggedResource);
		tagVotes.add(handTagging);
		handTaggingService = new HandTaggingService(handTaggingDAO, frontendContentUpdater);
	}
	
	@Test
	public void clearingTagVotesClearAllVotesForThatTagFromTheDatabase() throws Exception {				
		Mockito.when(handTaggingDAO.getVotesForTag(tag)).thenReturn(tagVotes);
		handTaggingService.clearTaggingsForTag(tag);
		Mockito.verify(handTaggingDAO).delete(handTagging);
	}
	
	@Test
	public void clearingTagVotesShouldtriggerFrontendContentUpdateForTheEffectedResources() throws Exception {
		Mockito.when(handTaggingDAO.getVotesForTag(tag)).thenReturn(tagVotes);
		handTaggingService.clearTaggingsForTag(tag);	
		Mockito.verify(frontendContentUpdater).update(taggedResource);		
	}
	
}
