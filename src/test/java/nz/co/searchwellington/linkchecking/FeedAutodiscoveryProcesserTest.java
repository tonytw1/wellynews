package nz.co.searchwellington.linkchecking;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService;
import nz.co.searchwellington.commentfeeds.CommentFeedGuesserService;
import nz.co.searchwellington.htmlparsing.LinkExtractor;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceFactory;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FeedAutodiscoveryProcesserTest {

	private static final String UNSEEN_FEED_URL = "http://something/new";
	private static final String EXISTING_FEED_URL = "http://something/old";
	
	@Mock ResourceRepository resourceDAO;
	@Mock LinkExtractor linkExtractor;
	@Mock CommentFeedDetectorService commentFeedDetector;
	@Mock CommentFeedGuesserService commentFeedGuesser;
	@Mock private ResourceFactory resourceFactory;
	
	@Mock Resource resource;
	@Mock Feed existingFeed;
	private String pageContent = "Meh";
	
	private FeedAutodiscoveryProcesser processor;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		processor = new FeedAutodiscoveryProcesser(resourceDAO, linkExtractor, commentFeedDetector, commentFeedGuesser, resourceFactory);		
		when(resource.getType()).thenReturn("N");
	}
	
	@Test
	public void newlyDiscoveredFeedsUrlsShouldBeRecordedAsDiscoveredFeeds() {
		Set<String> autoDiscoveredLinks = new HashSet<String>();
		autoDiscoveredLinks.add(UNSEEN_FEED_URL);
		
		when(linkExtractor.extractLinks(pageContent)).thenReturn(autoDiscoveredLinks);
		when(commentFeedDetector.isCommentFeedUrl(UNSEEN_FEED_URL)).thenReturn(false);
		
		when(resourceDAO.loadDiscoveredFeedByUrl(UNSEEN_FEED_URL)).thenReturn(null);
		
		DiscoveredFeed newlyDiscoveredFeed = new DiscoveredFeed();
		newlyDiscoveredFeed.setReferences(new HashSet<Resource>());
		when(resourceFactory.createNewDiscoveredFeed(UNSEEN_FEED_URL)).thenReturn(newlyDiscoveredFeed);
		
		processor.process(resource, pageContent);
		
		assertTrue(newlyDiscoveredFeed.getReferences().contains(resource));
		verify(resourceDAO).saveDiscoveredFeed(newlyDiscoveredFeed);
	}
	
	@Test
	public void doNotRecordDiscoveredFeedsIfWeAlreadyHaveThisFeed() {
		Set<String> autoDiscoveredLinks = new HashSet<String>();
		autoDiscoveredLinks.add(EXISTING_FEED_URL);
		
		when(linkExtractor.extractLinks(pageContent)).thenReturn(autoDiscoveredLinks);
		when(commentFeedDetector.isCommentFeedUrl(EXISTING_FEED_URL)).thenReturn(false);		
		when(resourceDAO.loadDiscoveredFeedByUrl(EXISTING_FEED_URL)).thenReturn(null);
		when(resourceDAO.loadFeedByUrl(EXISTING_FEED_URL)).thenReturn(existingFeed);
		
		processor.process(resource, pageContent);
		
		verify(resourceFactory, never()).createNewDiscoveredFeed(any(String.class));
		verify(resourceDAO, never()).saveDiscoveredFeed(any(DiscoveredFeed.class));
	}
	
}
	