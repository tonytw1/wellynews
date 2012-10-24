package nz.co.searchwellington.feeds;

import java.util.ArrayList;

import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.ConfigDAO;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class CommentFeedReaderTest {
	
	@Mock CommentFeedService commentFeedService;
	@Mock HibernateResourceDAO resourceDAO;
	@Mock ContentUpdateService contentUpdateService;
	@Mock ConfigDAO configDAO;
	
	private ArrayList<CommentFeed> commentFeedsToCheck;
	@Mock CommentFeed commentFeed;
	@Mock Newsitem newsitem;

	private CommentFeedReader commentFeedReader;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(configDAO.isFeedReadingEnabled()).thenReturn(true);
		
		Mockito.when(commentFeed.getNewsitem()).thenReturn(newsitem);
		commentFeedsToCheck = Lists.newArrayList();
		commentFeedsToCheck.add(commentFeed);
		for (int i = 0; i < 29; i++) {
			commentFeedsToCheck.add(new CommentFeed(0, null, Lists.<Comment>newArrayList(), null, null, null));
		}		
		Mockito.when(resourceDAO.getCommentFeedsToCheck(30)).thenReturn(commentFeedsToCheck);
		
		commentFeedReader = new CommentFeedReader(resourceDAO, commentFeedService, contentUpdateService, configDAO);
	}
    
	@Test
    public void shouldLimitTheNumberOfCommentsReadInOneGo() throws Exception {
		Mockito.when(resourceDAO.getCommentFeedsToCheck(30)).thenReturn(commentFeedsToCheck);
    	commentFeedReader.loadComments();
        Mockito.verify(commentFeedService).loadComments(commentFeed);
    }
    
	@Test
	public void shouldTriggerContentUpdateAfterReadingFeeds() throws Exception {
    	commentFeedReader.loadComments();
		Mockito.verify(contentUpdateService).update(newsitem);
	}
	
}
