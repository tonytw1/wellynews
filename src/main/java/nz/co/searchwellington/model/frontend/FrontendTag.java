package nz.co.searchwellington.model.frontend;

public class FrontendTag {
	
	private String id, name;
	
	public FrontendTag() {
		// TODO Auto-generated constructor stub
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "FrontendTag [id=" + id + ", name=" + name + "]";
	}
	
}
