package nz.co.searchwellington.model;

import java.util.Date;
import java.util.HashSet;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

public class SuggestionFeednewsitem extends ResourceImpl implements RssFeedable, Resource {

	private Suggestion suggestion;
	
	public SuggestionFeednewsitem(Suggestion suggestion, String name, Date date, String description) {
		super();
		this.suggestion = suggestion;
		this.name = name;
		this.date = date;
		this.tags = new HashSet<Tag>();		
		this.description = description;
	}
	
	final public String getType() {
		return "S";
	}
		
	@Override
	public String getUrl() {
		return suggestion.getUrl();
	}
		
	@Override
	public SyndEntry getRssItem() {
		SyndEntry entry = new SyndEntryImpl();      
		entry.setTitle(this.name);
		entry.setLink(this.getUrl());
		entry.setTitle(this.name);
		entry.setPublishedDate(this.date);
		return entry;
	}

	public Suggestion getSuggestion() {
		return suggestion;
	}
	
	public Date getFirstSeen() {
		return suggestion.getFirstSeen();
	}

	public Date getEmbargoedUntil() {
		return null;
	}

	public void setEmbargoedUntil(Date embargoedUntil) {
	}

	public boolean isHeld() {
		return false;
	}

	public void setHeld(boolean held) {
	}
		
}
