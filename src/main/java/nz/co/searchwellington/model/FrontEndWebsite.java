package nz.co.searchwellington.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.frontend.FrontendResourceImpl;

public class FrontEndWebsite extends FrontendResourceImpl implements Website {

	Website website;
	private List<Tag> tags;
	
	public FrontEndWebsite(Website website) {
		this.website = website;
		tags = new ArrayList<Tag>();
	}
	
	public List<Tag> getTags() {
		return tags;
	}
	
	public void addTag(Tag tag) {
		tags.add(tag);
	}
	
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
		
	public Set<CalendarFeed> getCalendars() {
		return website.getCalendars();
	}
	public Set<Feed> getFeeds() {
		return website.getFeeds();
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
	public Date getDate() {
		return website.getDate();
	}
	public String getDescription() {
		return website.getDescription();
	}
	public Set<DiscoveredFeed> getDiscoveredFeeds() {
		return website.getDiscoveredFeeds();
	}
	public Date getEmbargoedUntil() {
		return website.getEmbargoedUntil();
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
	public String getName() {
		return website.getName();
	}
	public User getOwner() {
		return website.getOwner();
	}
	public String getType() {
		return website.getType();
	}
	public String getUrl() {
		return website.getUrl();
	}
	public boolean isHeld() {
		return website.isHeld();
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
	public void setEmbargoedUntil(Date embargoedUntil) {
		website.setEmbargoedUntil(embargoedUntil);
	}
	public void setGeocode(Geocode geocode) {
		website.setGeocode(geocode);
	}
	public void setHeld(boolean held) {
		website.setHeld(held);
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
	public void setName(String name) {
		website.setName(name);
	}
	public void setOwner(User owner) {
		website.setOwner(owner);
	}
	public void setUrl(String url) {
		website.setUrl(url);
	}
	public void setUrlWords(String urlWords) {
		website.setUrlWords(urlWords);
	}
	
}
