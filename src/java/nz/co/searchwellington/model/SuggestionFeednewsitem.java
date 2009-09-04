package nz.co.searchwellington.model;

import java.util.Date;

import com.sun.syndication.feed.synd.SyndEntry;

public class SuggestionFeednewsitem extends Suggestion {

	private String title;
	private Date date;
	
	public SuggestionFeednewsitem(Suggestion suggestion, String title, Date date) {
		super(suggestion.feed, suggestion.getUrl(), suggestion.getFirstSeen());
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

	@Override
	public SyndEntry getRssItem() {
		SyndEntry item = super.getRssItem();
		item.setTitle(this.title);
		item.setPublishedDate(this.date);
		return item;
	}
	
	
	
}
