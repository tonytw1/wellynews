package nz.co.searchwellington.filters.attributesetters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;

public class CombinerPageAttributeSetter implements AttributeSetter {
	
	private static Logger log = Logger.getLogger(CombinerPageAttributeSetter.class);
	
	private static Pattern combinerPattern = Pattern.compile("^/(.*)\\+(.*?)(/rss|/json)?$");

	private TagDAO tagDAO;
	private ResourceRepository resourceDAO;
	
	public CombinerPageAttributeSetter(TagDAO tagDAO, ResourceRepository resourceDAO) {
		this.tagDAO = tagDAO;
		this.resourceDAO = resourceDAO;
	}
	
	public boolean setAttributes(HttpServletRequest request) {
		log.debug("Checking for combiner url: " + request.getPathInfo());
		Matcher matcher = combinerPattern.matcher(request.getPathInfo());
		if (matcher.matches()) {
			final String left = matcher.group(1);
			final String right = matcher.group(2);
			log.debug("Path matches combiner pattern for '" + left + "', '" + right + "'");
			// righthand side is always a tag;
			// Left could be a publisher or a tag.
			Tag rightHandTag = tagDAO.loadTagByName(right);
			if (rightHandTag != null) {
				Website publisher = resourceDAO.getPublisherByUrlWords(left);
				log.debug("Right matches tag: " + rightHandTag.getName());
				if (publisher != null) {
					log.debug("Left matches publisher: " + publisher.getName());
					request.setAttribute("publisher", publisher);
					request.setAttribute("tag", rightHandTag);
					return true;

				} else {
					Tag leftHandTag = tagDAO.loadTagByName(left);
					if (leftHandTag != null) {
						log.debug("Left matches tag: " + leftHandTag.getName());
						log.info("Setting tags '" + leftHandTag.getName()
								+ "', '" + rightHandTag.getName() + "'");
						List<Tag> tags = new ArrayList<Tag>();
						tags.add(leftHandTag);
						tags.add(rightHandTag);
						request.setAttribute("tags", tags);
						return true;
					}
				}
			}
		}
		return false;
	}
	
}