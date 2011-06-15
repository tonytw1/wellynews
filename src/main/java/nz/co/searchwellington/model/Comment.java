package nz.co.searchwellington.model;

public class Comment {

    private int id;
    private String title;
    
    public Comment() {
    }
    
    public Comment(String title) {
		this.title = title;
	}

	public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
}
