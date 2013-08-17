package nz.co.searchwellington.model;

import java.util.Date;
import java.util.List;

import uk.co.eelpieconsulting.common.geo.model.Place;

import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendTag;

public class FrontendFeedNewsitem implements FrontendNewsitem {

	private static final long serialVersionUID = 1L;

	FeedNewsitem feedNewsitem;
	private boolean isSuppressed;
	private Suggestion suggestion;
	Integer localCopy;
	private String urlWords;
	
    public FrontendFeedNewsitem(FeedNewsitem feedNewsitem) {
    	this.feedNewsitem = feedNewsitem;
        this.isSuppressed = false;
    }
    
	public Integer getLocalCopy() {
		return localCopy;
	}
	
	public void setLocalCopy(Integer localCopy) {
		this.localCopy = localCopy;
	}
	
	public boolean isSuppressed() {
		return isSuppressed;
	}

	public void setSuppressed(boolean isSuppressed) {
		this.isSuppressed = isSuppressed;
	}

	public Suggestion getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(Suggestion suggestion) {
		this.suggestion = suggestion;
	}

	public String getPublisherName() {
		return feedNewsitem.getPublisherName();
	}

	public final int getId() {
		return feedNewsitem.getId();
	}

	public final String getName() {
		return feedNewsitem.getName();
	}

	public final String getUrl() {
		return feedNewsitem.getUrl();
	}

	public final Date getDate() {
		return feedNewsitem.getDate();
	}

	public final String getDescription() {
		return feedNewsitem.getDescription();
	}

	public final Geocode getGeocode() {
		return feedNewsitem.getGeocode();
	}
	
	@Override
	public String getType() {
		return feedNewsitem.getType();
	}

	@Override
	public int getHttpStatus() {
		return feedNewsitem.getHttpStatus();
	}

	@Override
	public Date getLiveTime() {
		return feedNewsitem.getLiveTime();
	}

	@Override
	public List<FrontendTag> getTags() {
		return feedNewsitem.getTags();

	}

	@Override
	public List<Tag> getHandTags() {
		return feedNewsitem.getHandTags();
	}

	@Override
	public List<Twit> getRetweets() {
		return feedNewsitem.getRetweets();
	}

	@Override
	public String getAcceptedFromFeedName() {
		return feedNewsitem.getAcceptedFromFeedName();
	}

	@Override
	public String getAcceptedByProfilename() {
		return feedNewsitem.getAcceptedByProfilename();
	}

	@Override
	public List<Comment> getComments() {
		return feedNewsitem.getComments();
	}

	@Override
	public Date getAccepted() {
		return feedNewsitem.getAccepted();
	}

	@Override
	public Integer getOwnerId() {
		return null;
	}

	public String getUrlWords() {
		return urlWords;
	}

	public void setUrlWords(String urlWords) {
		this.urlWords = urlWords;
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

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}
	
}