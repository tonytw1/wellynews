package nz.co.searchwellington.model;

public class HandTag {

	Resource resource;
	User user;
	Tag tag;
	
	
	public HandTag(Resource resource, User user, Tag tag) {
		this.resource = resource;
		this.user = user;
		this.tag = tag;
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
	
}
