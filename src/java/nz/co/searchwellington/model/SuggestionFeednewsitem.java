package nz.co.searchwellington.model;

import java.util.Date;

import com.sun.syndication.feed.synd.SyndEntry;

public class SuggestionFeednewsitem extends Suggestion {

	private String name;
	private Date date;
	
	public SuggestionFeednewsitem(Suggestion suggestion, String name, Date date) {
		super(suggestion.feed, suggestion.getUrl(), suggestion.getFirstSeen());
		this.name = name;
		this.date = date;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
		item.setTitle(this.name);
		item.setPublishedDate(this.date);
		return item;
	}
	
	
	
}
