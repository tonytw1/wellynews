package nz.co.searchwellington.utils;

import nz.co.searchwellington.http.HttpFetcher;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RobotExclusionService {

	private static Logger log = Logger.getLogger(RobotExclusionService.class);

	private HttpFetcher httpFetcher;
	
	@Autowired
	public RobotExclusionService(HttpFetcher httpFetcher) {
		this.httpFetcher = httpFetcher;
	}
	
	public boolean isUrlCrawlable(String url, String userAgent) {
		return true;    // TODO not implemented
	}

}
