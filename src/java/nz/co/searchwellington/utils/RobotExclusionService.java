package nz.co.searchwellington.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import net.sourceforge.jrobotx.RobotExclusion;

public class RobotExclusionService {

	Logger log = Logger.getLogger(RobotExclusionService.class);
	
	public boolean isUrlCrawlable(String url) {		
		RobotExclusion jrobotx = new RobotExclusion();
		try {
			boolean isCrawlable = jrobotx.allows(new URL(url), HttpFetcher.USER_AGENT);
			log.info(url + "' is crawlable: " + isCrawlable);
			return isCrawlable;
		} catch (MalformedURLException e) {
			return false;
		}
	}

}
