package nz.co.searchwellington.filters.attributesetters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;

public class PublisherPageAttributeSetter implements AttributeSetter {
	
	private static Logger log = Logger.getLogger(PublisherPageAttributeSetter.class);
	
	private static Pattern publisherPagePathPattern = Pattern.compile("^/(.*?)(/(comment|geotagged))?(/(edit|save|rss|json))?$");
	
	private HibernateResourceDAO resourceDAO;
	
	public PublisherPageAttributeSetter(HibernateResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}
	
	@Override
	public boolean setAttributes(HttpServletRequest request) {
		log.debug("Looking for single publisher path");
		Matcher contentMatcher = publisherPagePathPattern.matcher(request.getPathInfo());
		if (contentMatcher.matches()) {
			final String match = contentMatcher.group(1);
			log.debug("'" + match + "' matches content");
			log.debug("Looking for publisher '" + match + "'");
			Website publisher = (Website) resourceDAO.getPublisherByUrlWords(match);
			if (publisher != null) {
				log.info("Setting publisher: " + publisher.getName());
				request.setAttribute("publisher", publisher);
				request.setAttribute("resource", publisher);
				return true;			
			}
		}
		return false;
	}
	
}
