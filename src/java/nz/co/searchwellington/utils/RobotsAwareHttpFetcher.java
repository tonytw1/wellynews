package nz.co.searchwellington.utils;

import java.io.InputStream;

// TODO want delegate, not extends
public class RobotsAwareHttpFetcher extends HttpFetcher {
	
	private RobotExclusionService robotExclusionService;
	private HttpFetcher httpFetcher;
		
	public RobotsAwareHttpFetcher(RobotExclusionService robotExclusionService, HttpFetcher httpFetcher) {		
		this.robotExclusionService = robotExclusionService;
		this.httpFetcher = httpFetcher;
	}

	public int httpFetch(String url, InputStream stream) {
		if (robotExclusionService.isUrlCrawlable(url)) {
			return httpFetcher.httpFetch(url, stream);
		}
		return -2;
	}
	
}
