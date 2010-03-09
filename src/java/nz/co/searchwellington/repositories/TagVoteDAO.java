package nz.co.searchwellington.repositories;

import java.util.Set;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;

public class TagVoteDAO {

    // TODO this is how we want to migrate to move from tags to tag votes.
	public Set<Tag> getHandpickerTagsForThisResourceByUser(User loggedInUser, Resource resource) {
		return resource.getTags();
	}

}
