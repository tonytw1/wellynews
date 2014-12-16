package nz.co.searchwellington.model.frontend;

public class FrontendTag {
	
	private final String id, name;
	
	public FrontendTag(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "FrontendTag [id=" + id + ", name=" + name + "]";
	}
	
}
