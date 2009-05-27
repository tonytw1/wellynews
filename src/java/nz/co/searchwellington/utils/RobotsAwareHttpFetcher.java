package nz.co.searchwellington.utils;

public class RobotsAwareHttpFetcher implements HttpFetcher {
	
	private RobotExclusionService robotExclusionService;
	private StandardHttpFetcher httpFetcher;
		
	public RobotsAwareHttpFetcher(RobotExclusionService robotExclusionService, StandardHttpFetcher httpFetcher) {		
		this.robotExclusionService = robotExclusionService;
		this.httpFetcher = httpFetcher;
	}

	public HttpFetchResult httpFetch(String url) {
		if (robotExclusionService.isUrlCrawlable(url, httpFetcher.getUserAgent())) {
			return httpFetcher.httpFetch(url);
		}
		return new HttpFetchResult(-2, null);
	}
	
}
