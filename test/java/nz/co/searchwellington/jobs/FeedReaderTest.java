package nz.co.searchwellington.jobs;

import junit.framework.TestCase;

public class FeedReaderTest extends TestCase {
    
    
    
    public void testname() throws Exception {
        
    }
   
//    
//    public void testShouldLimitTheNumberOfCommentsReadInOneGo() throws Exception {
//        
//        ArrayList<CommentFeed> commentFeeds = new ArrayList<CommentFeed>();
//        for (int i = 0; i < 100; i++) {
//            CommentFeed commentFeed = new CommentFeedImpl(0, null, new ArrayList<Comment>(), null, null, null);
//            commentFeeds.add(commentFeed);
//        }
//        
//        CommentDAO commentDAOMock = createMock(CommentDAO.class);
//        expect(commentDAOMock.loadComments(isA(CommentFeed.class))).andReturn(Collections.<Comment>emptyList()).times(1, 20);
//        replay(commentDAOMock);
//                
//        ResourceRepository resourceDAOMock = createMock(ResourceRepository.class);
//        expect(resourceDAOMock.getAllCommentFeeds()).andReturn(commentFeeds);
//        replay(resourceDAOMock);
//        
//        
//        
//        this.resourceDAO = resourceDAO;
//        this.feedDAO = feedDAO;
//        this.linkCheckerQueue = linkCheckerQueue;
//        this.placeAutoTagger = placeAutoTagger;
//        this.siteInformation = siteInformation;
//        this.notifier = notifier;
//        this.notificationReciept = notificationReciept;
//        this.feedAcceptanceDecider = feedAcceptanceDecider;
//        
//                
//        FeedReader feedReader = new FeedReader(resourceDAOMock, null, null, null, commentDAOMock, null, null, null);   
//        feedReader.loadComments();
//    }
//    
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
