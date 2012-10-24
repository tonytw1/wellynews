package nz.co.searchwellington.filters.attributesetters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

public class TagPageAttributeSetter implements AttributeSetter {
	
	private static Logger log = Logger.getLogger(TagPageAttributeSetter.class);
	
	private static Pattern tagPagePathPattern = Pattern.compile("^/(.*?)(/(comment|geotagged|autotag))?(/(rss|json))?$");
	
	private TagDAO tagDAO;
	
	public TagPageAttributeSetter(TagDAO tagDAO) {
		this.tagDAO = tagDAO;
	}
	
	@Override
	public boolean setAttributes(HttpServletRequest request) {
		log.debug("Looking for single tag path");
		Matcher contentMatcher = tagPagePathPattern.matcher(request.getPathInfo());
		if (contentMatcher.matches()) {
			final String match = contentMatcher.group(1);

			if (!isReservedUrlWord(match)) {
				log.debug("'" + match + "' matches content");

				log.debug("Looking for tag '" + match + "'");
				Tag tag = tagDAO.loadTagByName(match);
				if (tag != null) {
					log.info("Setting tag: " + tag.getName());
					request.setAttribute("tag", tag); // TODO deprecate
					List<Tag> tags = Lists.newArrayList();
					tags.add(tag);
					log.info("Setting tags: " + tags);
					request.setAttribute("tags", tags);
					return true;
				}
			}
		}
		return false;
	}
	
	// TODO this wants to be in the spring config
	// TODO Push up
	private boolean isReservedUrlWord(String urlWord) {
    	Set<String> reservedUrlWords = new HashSet<String>();
    	reservedUrlWords.add("about");
    	reservedUrlWords.add("api");
    	reservedUrlWords.add("autotag");
       	reservedUrlWords.add("index");
    	reservedUrlWords.add("feeds");
    	reservedUrlWords.add("comment");
    	reservedUrlWords.add("geotagged");
    	reservedUrlWords.add("tags");
    	return reservedUrlWords.contains(urlWord);
	}
	
}
