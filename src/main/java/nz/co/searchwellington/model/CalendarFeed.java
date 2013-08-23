package nz.co.searchwellington.model;


public class CalendarFeed extends PublishedResourceImpl implements PublishedResource {
	
	private static final long serialVersionUID = 1L;

	public CalendarFeed() {     
    }
	
	public CalendarFeed(int id, String url, String name, String description) {
        this.id = id;
        this.url = url;        
        this.name = name;
        this.description = description;
    }
	
    public String getType() {
		return "C";
	}
    
}
