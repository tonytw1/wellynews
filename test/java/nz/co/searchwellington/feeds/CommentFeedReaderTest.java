package nz.co.searchwellington.feeds;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.repositories.CommentDAO;
import nz.co.searchwellington.repositories.ResourceRepository;
public class CommentFeedReaderTest extends TestCase {
	

    // TODO can't implement with out an association between commentFeed and newsitems.    
    public void testShouldSaveNewsitemWhenItsCommentFeedIsLoadedToEnsureTheLuceneIndexIsUpdate() throws Exception {        
        ResourceRepository resourceDAO = mock(ResourceRepository.class);
        CommentDAO commentDAO = mock(CommentDAO.class);
        
        CommentFeed commentFeed = new CommentFeed();        
        commentFeed.setComments(new ArrayList<Comment>());
                
        CommentFeedReader commentFeedReader = new CommentFeedReader(resourceDAO, commentDAO);
        //commentFeedReader.loadCommentsFromCommentFeed(commentFeed);
        fail();
    }
    
}
