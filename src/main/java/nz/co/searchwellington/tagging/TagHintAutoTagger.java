package nz.co.searchwellington.tagging;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagHintAutoTagger {

	private static Logger log = Logger.getLogger(TagHintAutoTagger.class);

	private TagDAO tagDAO;

	@Autowired
	public TagHintAutoTagger(TagDAO tagDAO) {
		this.tagDAO = tagDAO;
	}

	public Set<Tag> suggestTags(Resource resource) {
		Set<Tag> suggestedTags = new HashSet<Tag>();

		List<Tag> tags = tagDAO.getAllTags();
		for (Tag tag : tags) {
			String tagHints = tag.getAutotagHints();
			if (tagHints != null && !tagHints.trim().equals("")) {
				String[] hints = tagHints.split(",");
				if (hints.length > 0) {
					for (int i = 0; i < hints.length; i++) {
						String hint = hints[i].trim();
						if (checkForMatch(resource, hint)) {
							log.info("Suggesting tag '" + tag.getDisplayName() + "' for resource: " + resource.getName());
							suggestedTags.add(tag);
						}
					}
				}
			}
		}
		return suggestedTags;

	}

	private boolean checkForMatch(Resource resource, String hint) {
		if (!hint.trim().equals("")) {
			boolean headlineMatchesHint = resource.getName().toLowerCase().contains(hint.toLowerCase());
			boolean BodyMatchesTag = resource.getDescription().toLowerCase().contains(hint.toLowerCase());
			return headlineMatchesHint || BodyMatchesTag;
		}
		return false;
	}

}
