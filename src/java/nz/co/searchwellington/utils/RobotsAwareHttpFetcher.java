package nz.co.searchwellington.utils;


public class RobotsAwareHttpFetcher implements HttpFetcher {
	
	private RobotExclusionService robotExclusionService;
	private StandardHttpFetcher httpFetcher;
	private String[] exceptions;
		
	public RobotsAwareHttpFetcher(RobotExclusionService robotExclusionService, StandardHttpFetcher httpFetcher, String... exceptions) {
		this.robotExclusionService = robotExclusionService;
		this.httpFetcher = httpFetcher;
		this.exceptions = exceptions;
	}

	public HttpFetchResult httpFetch(String url) {		
		boolean overrideRobotDotTxt = false;
		for (String exception : exceptions) {
			if (url.startsWith(exception)) {
				overrideRobotDotTxt = true;
			}
		}
		
		if (overrideRobotDotTxt || robotExclusionService.isUrlCrawlable(url, httpFetcher.getUserAgent())) {
			return httpFetcher.httpFetch(url);
		}		
		return new HttpFetchResult(-2, null);
	}
	
}
