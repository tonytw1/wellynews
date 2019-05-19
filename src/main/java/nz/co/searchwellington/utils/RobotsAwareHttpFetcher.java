package nz.co.searchwellington.utils;

import nz.co.searchwellington.http.HttpFetcher;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;

public class RobotsAwareHttpFetcher implements HttpFetcher {
	
    private Logger log = Logger.getLogger(RobotsAwareHttpFetcher.class);

	private RobotExclusionService robotExclusionService;
	private HttpFetcher httpFetcher;
	private String[] exceptions;
		
	public RobotsAwareHttpFetcher(RobotExclusionService robotExclusionService, HttpFetcher httpFetcher, String... exceptions) {
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
		
		if (overrideRobotDotTxt || robotExclusionService.isUrlCrawlable(url, getUserAgent())) {
			return httpFetcher.httpFetch(url);
		}
		log.info("Url is not allowed to be crawled: " + url);
		return new HttpFetchResult(HttpStatus.SC_UNAUTHORIZED, null);
	}

	@Override
	public String getUserAgent() {
		return httpFetcher.getUserAgent();
	}

}
