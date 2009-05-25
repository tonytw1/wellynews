package nz.co.searchwellington.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.InputStream;

import junit.framework.TestCase;

public class RobotsAwareHttpFetcherTest extends TestCase {
	
	private static final String TEST_URL = "http://test.wellington.gen.nz/blah";
	
	private RobotExclusionService robotExclusionService = mock(RobotExclusionService.class);
	private HttpFetcher httpFetcher = mock(HttpFetcher.class);
	private HttpFetcher robotsAwareFetcher;
	private InputStream inputStream;
	
	@Override
	protected void setUp() throws Exception {		
		robotsAwareFetcher = new RobotsAwareHttpFetcher(robotExclusionService, httpFetcher);	
		inputStream = mock(InputStream.class);
	} 
	
	
	public void testMustCheckWithTheRobotsExclutionServiceBeforeCrawlingUrl() throws Exception {		
		robotsAwareFetcher.httpFetch(TEST_URL, inputStream);
		verify(robotExclusionService).isUrlCrawlable(TEST_URL);
	}
	
	public void testShouldCrawlIfFetcherSaysOk() throws Exception {
		stub(robotExclusionService.isUrlCrawlable(TEST_URL)).toReturn(true);
		robotsAwareFetcher.httpFetch(TEST_URL, inputStream);
		verify(httpFetcher).httpFetch(TEST_URL, inputStream);
	}
	
	public void testShouldNotCrawlIfFetcherSaysNo() throws Exception {
		stub(robotExclusionService.isUrlCrawlable(TEST_URL)).toReturn(false);
		robotsAwareFetcher.httpFetch(TEST_URL, inputStream);
		verifyNoMoreInteractions(httpFetcher);
	}
	
	public void testShouldGiveCorrectStatusCodeIfCrawlsAreNotAllowed() throws Exception {
		stub(robotExclusionService.isUrlCrawlable(TEST_URL)).toReturn(false);
		final int status = robotsAwareFetcher.httpFetch(TEST_URL, inputStream);
		assertEquals(-2, status);
	}
	
}
