package nz.co.searchwellington.model.decoraters.highlighting;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;

public class SolrHighlightingWebsiteDecorator implements Website {

	private Website website;
	private Map<String, List<String>> map;
      
    public SolrHighlightingWebsiteDecorator(Website website, Map<String, List<String>> map) {
    	this.website = website;
    	this.map = map;
    }
    
    public String getName() {
    	if (map.containsKey("name")) {
    		return map.get("name").get(0);
    	}
    	return website.getName();	
    }

    public String getDescription() {
    	if (map.containsKey("description")) {
    		return map.get("description").get(0);
    	}
    	return website.getDescription();
    }
    
    public List<String> getBodytext() {    	
    	if (map.containsKey("bodytext")) {
			return map.get("bodytext");
		}
		return null;
	}
    
    
	public void addTag(Tag tag) {
		website.addTag(tag);
	}
	
	

	public void clearTags() {
		website.clearTags();
	}

	public Set<CalendarFeed> getCalendars() {
		return website.getCalendars();
	}

	public Date getDate() {
		return website.getDate();
	}


	public Set<DiscoveredFeed> getDiscoveredFeeds() {
		return website.getDiscoveredFeeds();
	}

	public Set<Feed> getFeeds() {
		return website.getFeeds();
	}

	public Geocode getGeocode() {
		return website.getGeocode();
	}

	public int getHttpStatus() {
		return website.getHttpStatus();
	}

	public int getId() {
		return website.getId();
	}

	public Date getLastChanged() {
		return website.getLastChanged();
	}

	public Date getLastScanned() {
		return website.getLastScanned();
	}

	public Date getLiveTime() {
		return website.getLiveTime();
	}


	public Set<Newsitem> getNewsitems() {
		return website.getNewsitems();
	}

	public void getRemoveTag(Tag tag) {
		website.getRemoveTag(tag);
	}
	
	public Set<Tag> getTags() {
		return website.getTags();
	}

	public String getType() {
		return website.getType();
	}

	public String getUrl() {
		return website.getUrl();
	}

	public String getUrlWords() {
		return website.getUrlWords();
	}

	public Set<Watchlist> getWatchlist() {
		return website.getWatchlist();
	}

	public void setCalendars(Set<CalendarFeed> calendars) {
		website.setCalendars(calendars);
	}

	public void setDate(Date date) {
		website.setDate(date);
	}

	public void setDescription(String description) {
		website.setDescription(description);
	}

	public void setDiscoveredFeeds(Set<DiscoveredFeed> discoveredFeeds) {
		website.setDiscoveredFeeds(discoveredFeeds);
	}

	public void setGeocode(Geocode geocode) {
		website.setGeocode(geocode);
	}

	public void setHttpStatus(int httpStatus) {
		website.setHttpStatus(httpStatus);
	}

	public void setId(int id) {
		website.setId(id);
	}

	public void setLastChanged(Date lastChanged) {
		website.setLastChanged(lastChanged);
	}

	public void setLastScanned(Date lastScanned) {
		website.setLastScanned(lastScanned);
	}

	public void setLiveTime(Date time) {
		website.setLiveTime(time);
	}

	
	
	public Date getEmbargoedUntil() {
		return website.getEmbargoedUntil();
	}

	public void setEmbargoedUntil(Date embargoedUntil) {
		website.setEmbargoedUntil(embargoedUntil);
	}

	public void setName(String name) {
		website.setName(name);
	}

	public void setTags(Set<Tag> tags) {
		website.setTags(tags);
	}

	public void setUrl(String url) {
		website.setUrl(url);
	}

	public void setUrlWords(String urlWords) {
		website.setUrlWords(urlWords);
	}

	public User getOwner() {
		return website.getOwner();
	}

	public void setOwner(User owner) {
		website.setOwner(owner);
	}

	public boolean isHeld() {
		return website.isHeld();
	}

	public void setHeld(boolean held) {
		website.setHeld(held);
	}
	
	
	
	
}
