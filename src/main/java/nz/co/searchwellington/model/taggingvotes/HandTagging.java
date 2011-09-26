package nz.co.searchwellington.model.taggingvotes;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.taggingvotes.voters.TaggingVoter;

public class HandTagging implements TaggingVote {

	private int id;
	private Resource resource;
	private User user;
	private Tag tag;
		
	public HandTagging() {
	}
	
	public HandTagging(int id, Resource resource, User user, Tag tag) {
		this.id = id;
		this.resource = resource;
		this.user = user;
		this.tag = tag;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public Tag getTag() {
		return tag;
	}
	
	public void setTag(Tag tag) {
		this.tag = tag;
	}

	@Override
	public TaggingVoter getVoter() {
		return user;
	}
	
}
