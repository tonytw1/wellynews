package nz.co.searchwellington.model;

import java.util.Date;

public class SuggestionFeednewsitem extends Suggestion {

	private String title;
	private Date date;
	
	public SuggestionFeednewsitem(Feed feed, String url, String title, Date date) {
		super(feed, url);
		this.title = title;
		this.date = date;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
