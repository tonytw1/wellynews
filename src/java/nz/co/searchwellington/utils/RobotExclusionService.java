package nz.co.searchwellington.utils;

import java.net.URL;

import net.sourceforge.jrobotx.RobotExclusion;

import org.apache.log4j.Logger;

public class RobotExclusionService {

	Logger log = Logger.getLogger(RobotExclusionService.class);
	
	public boolean isUrlCrawlable(String url, String userAgent) {
		log.info("Checking if url '" + url + "' is allowed for user agent '" + userAgent + "'");
		RobotExclusion jrobotx = new RobotExclusion();
		try {
			boolean isCrawlable = jrobotx.allows(new URL(url), userAgent);
			log.info(url + "' is crawlable: " + isCrawlable);
			return isCrawlable;
		} catch (Exception e) {
			log.warn(url + " caused an exception: " + e);
			return false;
		}
	}

}
