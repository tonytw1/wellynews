package nz.co.searchwellington.jobs;

//import static org.mockito.Mockito.mock;
import junit.framework.TestCase;
import nz.co.searchwellington.model.LinkCheckerQueue;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.twitter.TwitterNewsitemMentionsFinderService;
import nz.co.searchwellington.twitter.TwitterService;

public class TwitterListenerJobTest extends TestCase {

	private TwitterService twitterService;
	private TwitterNewsitemMentionsFinderService twitterNewsitemBuilder;
	private ResourceRepository resourceDAO;
	private LinkCheckerQueue linkCheckerQueue;

	
	 @Override
	    protected void setUp() throws Exception {
//	    	twitterService = mock(TwitterService.class);
//	        twitterNewsitemBuilder = mock(TwitterNewsitemBuilderService.class);
//	        resourceDAO = mock(ResourceRepository.class);
//	        linkCheckerQueue = mock(LinkCheckerQueue.class);
	        
	        
	    }
	
	public void testShouldIgnoreRTs() throws Exception {		
//		TwitterListenerJob twitterListener = new TwitterListenerJob(twitterService, twitterNewsitemBuilder, resourceDAO, linkCheckerQueue);
		
	//	Status rtReply = mock(Status.class);
	//	Status[] replies = new Status[1];
	//	replies[0] = rtReply;		
	//	stub(twitterService.getReplies()).toReturn(replies);
		fail();
	}
}
