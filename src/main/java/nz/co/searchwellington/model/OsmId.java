package nz.co.searchwellington.model;

public class OsmId {
	
	private Long id;
	private String type;
	
	public OsmId(Long id, String type) {		
		this.id = id;
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "OsmId [id=" + id + ", type=" + type + "]";
	}
	
}
