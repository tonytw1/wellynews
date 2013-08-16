package nz.co.searchwellington.model;

import uk.co.eelpieconsulting.common.geo.model.Place;

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

	@Override
	public Place getPlace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPlace(Place place) {
		// TODO Auto-generated method stub
		
	}
    
}
