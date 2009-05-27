package nz.co.searchwellington.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import net.sourceforge.jrobotx.RobotExclusion;

public class RobotExclusionService {

	Logger log = Logger.getLogger(RobotExclusionService.class);
	
	public boolean isUrlCrawlable(String url, String userAgent) {
		log.info("Checking if url '" + url + "' is allowed for user agent '" + userAgent + "'");
		RobotExclusion jrobotx = new RobotExclusion();
		try {
			boolean isCrawlable = jrobotx.allows(new URL(url), userAgent);
			log.info(url + "' is crawlable: " + isCrawlable);
			return isCrawlable;
		} catch (MalformedURLException e) {
			log.warn(url + " caused a MalformedUrlException");
			return false;
		}
	}

}
