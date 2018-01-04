package nz.co.searchwellington.utils;

import com.trigonic.jrobotx.RobotExclusion;
import com.trigonic.jrobotx.util.URLInputStreamFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class RobotExclusionService {

	private static Logger log = Logger.getLogger(RobotExclusionService.class);

	private StandardHttpFetcher httpFetcher;
	
	@Autowired
	public RobotExclusionService(StandardHttpFetcher httpFetcher) {
		this.httpFetcher = httpFetcher;
	}
	
	public boolean isUrlCrawlable(String url, String userAgent) {
		log.info("Checking if url '" + url + "' is allowed for user agent '" + userAgent + "'");
		URLInputStreamFactory robotsDotTextInputStream = new HttpFetcherUrlInputStreamFactory(httpFetcher);
		RobotExclusion jrobotx = new RobotExclusion(robotsDotTextInputStream);
		try {
			boolean isCrawlable = jrobotx.allows(new URL(url), userAgent);
			log.info(url + "' is crawlable: " + isCrawlable);
			return isCrawlable;
		} catch (Exception e) {
			log.warn(url + " caused an exception, marking as uncrawlable until this is resolved: " + e);
			return false;
		}
	}

}
