package nz.co.searchwellington.utils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import nz.co.searchwellington.http.HttpFetcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RobotsAwareHttpFetcherTest extends TestCase {
	
	private static final String TEST_URL = "http://test.wellington.gen.nz/blah";
	private static final String TEST_USER_AGENT = "test user agent";
	
	@Mock private RobotExclusionService robotExclusionService;
	@Mock private HttpFetcher httpFetcher;
	@Mock private HttpFetcher robotsAwareFetcher;
	
	@Override
	protected void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(httpFetcher.getUserAgent()).thenReturn(TEST_USER_AGENT);
		robotsAwareFetcher = new RobotsAwareHttpFetcher(robotExclusionService, httpFetcher);	
	} 
	
	public void testMustCheckWithTheRobotsExclutionServiceBeforeCrawlingUrl() throws Exception {		
		robotsAwareFetcher.httpFetch(TEST_URL);
		verify(robotExclusionService).isUrlCrawlable(TEST_URL, TEST_USER_AGENT);
	}
	
	public void testShouldCrawlIfFetcherSaysOk() throws Exception {
		when(robotExclusionService.isUrlCrawlable(TEST_URL,TEST_USER_AGENT)).thenReturn(true);
		robotsAwareFetcher.httpFetch(TEST_URL);
		verify(httpFetcher).httpFetch(TEST_URL);
	}
	
	public void testShouldNotCrawlIfFetcherSaysNo() throws Exception {
		when(robotExclusionService.isUrlCrawlable(TEST_URL, TEST_USER_AGENT)).thenReturn(false);
		robotsAwareFetcher.httpFetch(TEST_URL);
		verify(httpFetcher).getUserAgent();
		verifyNoMoreInteractions(httpFetcher);
	}
	
	public void testShouldGiveCorrectStatusCodeIfCrawlsAreNotAllowed() throws Exception {
		when(robotExclusionService.isUrlCrawlable(TEST_URL, TEST_USER_AGENT)).thenReturn(false);
		HttpFetchResult result = robotsAwareFetcher.httpFetch(TEST_URL);
		assertEquals(401, result.getStatus());
	}
	
}
