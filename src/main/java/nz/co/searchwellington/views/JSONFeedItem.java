package nz.co.searchwellington.views;

import java.util.Date;

public class JSONFeedItem {
	
	String title;
	String url;
	Date date;
	String description;
	
	Double latitude;
	Double longitude;
	
	public JSONFeedItem(String title, String url, Date date, String description, Double latitude, Double longitude) {		
		this.title = title;
		this.url = url;
		this.date = date;
		this.description = description;
		this.latitude = latitude;
		this.longitude = longitude;
	}
		
}
