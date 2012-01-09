package nz.co.searchwellington.linkchecking;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService;
import nz.co.searchwellington.commentfeeds.CommentFeedGuesserService;
import nz.co.searchwellington.htmlparsing.LinkExtractor;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FeedAutodiscoveryProcesserTest {

	private static final String UNSEEN_FEED_URL = "http://something/new";
	
	@Mock ResourceRepository resourceDAO;
	@Mock LinkExtractor linkExtractor;
	@Mock CommentFeedDetectorService commentFeedDetector;
	@Mock CommentFeedGuesserService commentFeedGuesser;
	
	@Mock Resource resource;
	private String pageContent = "Meh";
	
	private FeedAutodiscoveryProcesser processor;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		processor = new FeedAutodiscoveryProcesser(resourceDAO, linkExtractor, commentFeedDetector, commentFeedGuesser);		
		when(resource.getType()).thenReturn("N");
	}
	
	@Test
	public void doNotRecordDiscoveredFeedsIfWeAlreadyHaveThisFeed() {
		Set<String> autoDiscoveredLinks = new HashSet<String>();
		autoDiscoveredLinks.add(UNSEEN_FEED_URL);
		
		when(linkExtractor.extractLinks(pageContent)).thenReturn(autoDiscoveredLinks);
		when(commentFeedDetector.isCommentFeedUrl(UNSEEN_FEED_URL)).thenReturn(false);
		
		when(resourceDAO.loadDiscoveredFeedByUrl(UNSEEN_FEED_URL)).thenReturn(null);
		
		DiscoveredFeed newlyDiscoveredFeed = new DiscoveredFeed();
		newlyDiscoveredFeed.setReferences(new HashSet<Resource>());
		when(resourceDAO.createNewDiscoveredFeed(UNSEEN_FEED_URL)).thenReturn(newlyDiscoveredFeed);
		
		processor.process(resource, pageContent);
		
		assertTrue(newlyDiscoveredFeed.getReferences().contains(resource));
		verify(resourceDAO).saveDiscoveredFeed(newlyDiscoveredFeed);
	}

}
