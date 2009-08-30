package nz.co.searchwellington.model;

public class Suggestion {
	
	private int id;
	private String url;
	
	
	
	public Suggestion() {	
	}


	public Suggestion(String url) {
		id = 0;
		this.url = url;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}
	
}
