package nz.co.searchwellington.feeds;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.repositories.ConfigRepository;
import nz.co.searchwellington.repositories.ResourceRepository;

public class CommentFeedReaderTest {
	
	private ArrayList<CommentFeed> commentFeeds;

	@Mock CommentFeedService commentFeedService;
	@Mock ResourceRepository resourceDAO;
	@Mock ContentUpdateService contentUpdateService;
	@Mock ConfigRepository configDAO;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(configDAO.isFeedReadingEnabled()).thenReturn(true);
		
		commentFeeds = new ArrayList<CommentFeed>();
		for (int i = 0; i < 100; i++) {
			CommentFeed commentFeed = new CommentFeed(0, null, new ArrayList<Comment>(), null, null, null);
			commentFeeds.add(commentFeed);
		}
	}
    
	@Test
    public void shouldLimitTheNumberOfCommentsReadInOneGo() throws Exception {
    	CommentFeedReader commentFeedReader = new CommentFeedReader(resourceDAO, commentFeedService, contentUpdateService, configDAO);
        
    	/*
        CommentDAO commentDAOMock = createMock(CommentDAO.class);
        expect(commentDAOMock.loadComments(isA(CommentFeed.class))).andReturn(Collections.<Comment>emptyList()).times(1, 20);
        replay(commentDAOMock);
                
        ResourceRepository resourceDAOMock = createMock(ResourceRepository.class);
        expect(resourceDAOMock.getAllCommentFeeds()).andReturn(commentFeeds);
        replay(resourceDAOMock);
        */
       
        commentFeedReader.loadComments();
        
        //Mockito.verify(commentFeedService.loadComments(Mockito.any(CommentFeed.class)), Mockito.times(30));
        fail();
    }
    
 /*   
	public void estShouldOnlyReadApproriateCommentFeeds() throws Exception {		
		CommentFeed readWithinTheLastTwoHours = new CommentFeedImpl(0, null, new ArrayList<Comment>(), null, null, null);	
		Calendar oneHourAgo = Calendar.getInstance();
		oneHourAgo.add(Calendar.HOUR, -1);		
		readWithinTheLastTwoHours.setLastRead(oneHourAgo.getTime());
	
		
		CommentFeed readOneYearAgo = new CommentFeedImpl(0, null, new ArrayList<Comment>(), null, null, null);	
		Calendar oneYearAgo = Calendar.getInstance();
		oneYearAgo.add(Calendar.YEAR, -1);		
		readOneYearAgo.setLastRead(oneYearAgo.getTime());
					
		CommentDAO commentDAOMock = createMock(CommentDAO.class);
		expect(commentDAOMock.loadComments(readOneYearAgo)).andReturn(Collections.<Comment>emptyList());
		replay(commentDAOMock);
		
		ResourceRepository resourceDAOMock = createMock(ResourceRepository.class);
		expect(resourceDAOMock.getAllCommentFeeds()).andReturn(Arrays.asList(readWithinTheLastTwoHours, readOneYearAgo));
		replay(resourceDAOMock);
				
		FeedReader feedReader = new FeedReader(resourceDAOMock, null, null, null, commentDAOMock, null, null, null);			
		feedReader.loadComments();
		
		verify(resourceDAOMock);
		verify(commentDAOMock);		
	}*/
	
}
