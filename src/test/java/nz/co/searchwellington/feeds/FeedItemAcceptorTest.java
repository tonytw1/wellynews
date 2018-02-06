package nz.co.searchwellington.feeds;

import java.sql.Date;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class FeedItemAcceptorTest {
	
	@Mock Newsitem feednewsitem;
	@Mock Feed feed;
	@Mock FrontendFeedNewsitem feedNewsitem;
	@Mock Website publisher;
	
	private FeedItemAcceptor feedItemAcceptor;
	private User user;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		//Mockito.when(feed.getPublisher()).thenReturn(publisher);
		Mockito.when(feed.getName()).thenReturn("A feed");
		Mockito.when(feednewsitem.getName()).thenReturn("HEADLINE");
		//Mockito.when(feednewsitem.getFeed()).thenReturn(feed);
		feedItemAcceptor = new FeedItemAcceptor();
	}
	
	@Test
	public void shouldSetAcceptedTimeWhenAccepting() throws Exception {
		feedItemAcceptor.acceptFeedItem(user, feednewsitem);
		Mockito.verify(feednewsitem).setAccepted(Mockito.any(Date.class));
	}
	
	@Test
	public void shouldSetAcceptedByUserAndOwnerWhenAccepting() throws Exception {
		feedItemAcceptor.acceptFeedItem(user, feednewsitem);
		//Mockito.verify(feednewsitem).setAcceptedBy(user);
		Mockito.verify(feednewsitem).setOwner(user);
	}
	
	@Test
	public void shouldFlattenLoudHeadlinesWhenAccepting() throws Exception {		
		feedItemAcceptor.acceptFeedItem(user, feednewsitem);
		Mockito.verify(feednewsitem).setName("Headline");
	}
	
}
