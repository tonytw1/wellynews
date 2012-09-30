package nz.co.searchwellington.filters.attributesetters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;

public class FeedAttributeSetter implements AttributeSetter {
	
	private static Logger log = Logger.getLogger(FeedAttributeSetter.class);
	
	public static final String FEED_ATTRIBUTE = "feedAttribute";

	private static Pattern feedPattern = Pattern.compile("^/feed/(.*?)(/(edit|save|rss|json))?$");

	private HibernateResourceDAO resourceDAO;
	
	public FeedAttributeSetter(HibernateResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}
	
	@Override
	public boolean setAttributes(HttpServletRequest request) {
		log.debug("Looking for feed path");
		Matcher contentMatcher = feedPattern.matcher(request.getPathInfo());
		if (contentMatcher.matches()) {
			final String match = contentMatcher.group(1);
			log.debug("'" + match + "' matches content");
			log.debug("Looking for feed '" + match + "'");
			Feed feed = (Feed) resourceDAO.loadFeedByUrlWords(match);
			if (feed != null) {
				log.info("Setting feed: " + feed.getName());
				request.setAttribute(FEED_ATTRIBUTE, feed);
				request.setAttribute("resource", feed);
				return true;
			}
		}
		return false;
	}
	
}
