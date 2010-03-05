package nz.co.searchwellington.model;

import java.util.Date;
import java.util.HashSet;

public class SuggestionFeednewsitem extends ResourceImpl implements Resource {

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
