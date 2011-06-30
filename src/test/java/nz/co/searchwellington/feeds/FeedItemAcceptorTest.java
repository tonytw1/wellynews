package nz.co.searchwellington.feeds;

import java.sql.Date;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.modification.ContentUpdateService;
import nz.co.searchwellington.tagging.AutoTaggingService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class FeedItemAcceptorTest {

	@Mock RssfeedNewsitemService rssfeedNewsitemService;
	@Mock AutoTaggingService autoTagger;   
	@Mock ContentUpdateService contentUpdateService;
	
	@Mock FeedNewsitem feednewsitem;
	@Mock Feed feed;
	@Mock FeedNewsitem feedNewsitem;
	@Mock Newsitem newsitem;
	@Mock Website publisher;
	
	private FeedItemAcceptor feedItemAcceptor;
	private User user;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(feednewsitem.getFeed()).thenReturn(feed);
		Mockito.when(feed.getPublisher()).thenReturn(publisher);
		Mockito.when(newsitem.getName()).thenReturn("HEADLINE");
		Mockito.when(rssfeedNewsitemService.makeNewsitemFromFeedItem(Mockito.any(FeedNewsitem.class))).thenReturn(newsitem);
		feedItemAcceptor = new FeedItemAcceptor(rssfeedNewsitemService, autoTagger, contentUpdateService);
	}
	
	@Test
	public void shouldSetAcceptedTimeWhenAccepting() throws Exception {
		feedItemAcceptor.acceptFeedItem(user, feednewsitem);
		Mockito.verify(newsitem).setAccepted(Mockito.any(Date.class));
	}
	
	@Test
	public void shouldSetAcceptedByUserWhenAccepting() throws Exception {
		feedItemAcceptor.acceptFeedItem(user, feednewsitem);
		Mockito.verify(newsitem).setAcceptedBy(user);
	}
	
	@Test
	public void shouldFlattenLoudHeadlinesWhenAccepting() throws Exception {		
		feedItemAcceptor.acceptFeedItem(user, feednewsitem);
		Mockito.verify(newsitem).setName("Headline");
	}
	
}
