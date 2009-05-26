package nz.co.searchwellington.controllers;

import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService;
import nz.co.searchwellington.feeds.CommentFeedReader;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.htmlparsing.CompositeLinkExtractor;
import nz.co.searchwellington.htmlparsing.LinkExtractor;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SnapshotDAO;
import nz.co.searchwellington.repositories.TechnoratiDAO;
import nz.co.searchwellington.utils.HttpFetchResult;
import nz.co.searchwellington.utils.HttpFetcher;
import nz.co.searchwellington.utils.RobotsAwareHttpFetcher;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class LinkCheckerNewTest extends TestCase {

	private static final int RESOURCE_ID = 123;
	private static final String RESOURCE_URL = "http://test.wellington.gen.nz";
	
	private ResourceRepository resourceDAO = mock(ResourceRepository.class);
    private RssfeedNewsitemService rssfeedNewsitemService = mock(RssfeedNewsitemService.class);
    private CommentFeedReader commentFeedReader = mock(CommentFeedReader.class);
	private CommentFeedDetectorService commentFeedDetector = mock(CommentFeedDetectorService.class);
	private SnapshotDAO snapshotDAO = mock(SnapshotDAO.class);
    private TechnoratiDAO technoratiDAO = mock(TechnoratiDAO.class);
	private HttpFetcher httpFetcher = mock(RobotsAwareHttpFetcher.class);
	private LinkExtractor linkExtractor = mock(CompositeLinkExtractor.class);

	
	public void testShouldRecordCorrectStatusForCrawlingDenied() throws Exception {		
		LinkChecker linkChecker = new LinkChecker(resourceDAO, rssfeedNewsitemService, commentFeedReader, commentFeedDetector, snapshotDAO, technoratiDAO, httpFetcher, linkExtractor);
		
		Resource resource = new NewsitemImpl();
		resource.setUrl(RESOURCE_URL);
		stub(resourceDAO.loadResourceById(RESOURCE_ID)).toReturn(resource);
		HttpFetchResult robotsDeniedHttpResult = new HttpFetchResult(-2, null);
		stub(httpFetcher.httpFetch(RESOURCE_URL)).toReturn(robotsDeniedHttpResult);
		
		linkChecker.scanResource(RESOURCE_ID);
		assertEquals(-2, resource.getHttpStatus());
	}
	
}
