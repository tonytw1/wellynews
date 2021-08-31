package nz.co.searchwellington.modification;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.HandTaggingService;
import nz.co.searchwellington.repositories.TagDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import scala.concurrent.Future;

public class TagModificationServiceTest {

    @Mock
    TagDAO tagDAO;
    @Mock
    HandTaggingService handTaggingService;

    @Mock
    Tag tag;

    private TagModificationService tagModificationService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(tag.getName()).thenReturn("Tag");
        tagModificationService = new TagModificationService(tagDAO, handTaggingService);
    }

    @Test
    public void tagDeletionShouldResultInTheRemovalOfTheTag() {
        Mockito.when(handTaggingService.clearTaggingsForTag(tag)).thenReturn(Future.successful(true));
        Mockito.when(tagDAO.deleteTag(tag)).thenReturn(Future.successful(true));

        tagModificationService.deleteTag(tag);

        Mockito.verify(tagDAO).deleteTag(tag);
    }

    @Test
    public void tagDeletionShouldResultInTheRemovalOfAllHandTaggingVotesForThatTag() {
        Mockito.when(handTaggingService.clearTaggingsForTag(tag)).thenReturn(Future.successful(true));
		Mockito.when(tagDAO.deleteTag(tag)).thenReturn(Future.successful(true));

        tagModificationService.deleteTag(tag);

        Mockito.verify(handTaggingService).clearTaggingsForTag(tag);
    }

}
