package nz.co.searchwellington.tagging;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.TagDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagHintAutoTagger {

	private static Logger log = Logger.getLogger(TagHintAutoTagger.class);

	private static final Splitter COMMA_SPLITTER = Splitter.on(",");

	private final TagDAO tagDAO;

	@Autowired
	public TagHintAutoTagger(TagDAO tagDAO) {
		this.tagDAO = tagDAO;
	}

	public Set<Tag> suggestTags(Resource resource) {
		final Set<Tag> suggestedTags = Sets.newHashSet();
		for (Tag tag : tagDAO.getAllTags()) {
			if (!Strings.isNullOrEmpty(tag.getAutotagHints())) {
				final List<String> hints = Lists.newArrayList(COMMA_SPLITTER.split(tag.getAutotagHints()));
				suggestedTags.addAll(process(resource, tag, hints));
			}
		}
		return suggestedTags;

	}

	private Set<Tag> process(Resource resource, Tag tag, List<String> hints) {
		final Set<Tag> suggestedTags = Sets.newHashSet();
		for(String hint : hints) {
			if (checkForMatch(resource, hint)) {
				log.info("Suggesting tag '" + tag.getDisplayName() + "' for resource: " + resource.getName());
				suggestedTags.add(tag);
            }
        }
		return suggestedTags;
	}

	private boolean checkForMatch(Resource resource, String hint) {
		final boolean headlineMatchesHint = resource.getName().toLowerCase().contains(hint.toLowerCase());
		final boolean bodyMatchesTag = resource.getDescription().toLowerCase().contains(hint.toLowerCase());
		return headlineMatchesHint || bodyMatchesTag;
	}

}
