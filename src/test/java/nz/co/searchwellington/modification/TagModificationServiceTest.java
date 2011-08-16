package nz.co.searchwellington.modification;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.HandTaggingDAO;
import nz.co.searchwellington.repositories.TagDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TagModificationServiceTest {

	@Mock TagDAO tagDAO;
	@Mock HandTaggingDAO handTaggingDAO;

	@Mock Tag tag;

	private TagModificationService tagModificationService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(tag.getName()).thenReturn("Tag");
		tagModificationService = new TagModificationService(tagDAO, handTaggingDAO);		
	}

	@Test
	public void tagDeletionShouldResultInTheRemovalOfTheTag() throws Exception {		
		tagModificationService.deleteTag(tag);		
		Mockito.verify(tagDAO).deleteTag(tag);
	}
	
	@Test
	public void tagDeletionShouldResultInTheRemovalOfAllHandTaggingVotesForThatTag() throws Exception {
		tagModificationService.deleteTag(tag);		
		Mockito.verify(handTaggingDAO).clearTaggingsForTag(tag);
	}
	
}
