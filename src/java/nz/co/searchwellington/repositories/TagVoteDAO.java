package nz.co.searchwellington.repositories;

import java.util.HashSet;
import java.util.Set;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;

public class TagVoteDAO {

    // TODO this is how we want to migrate to move from tags to tag votes.
	public Set<Tag> getHandpickerTagsForThisResourceByUser(User loggedInUser, Resource resource) {
		return new HashSet<Tag>(); // TODO implement
	}

	public void addTag(User loggedInUser, Tag tag, Resource resource) {
		// TODO Auto-generated method stub		
	}

	public Set<Tag> getHandTagsForResource(Resource resource) {
		// TODO Auto-generated method stub
		return null;
	}

}
