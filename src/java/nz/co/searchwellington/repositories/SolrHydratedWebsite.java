package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;
import java.util.Set;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Image;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;

import org.apache.commons.collections.set.ListOrderedSet;

public class SolrHydratedWebsite implements Website {
	
	int id;
	String headline;
	String description;
	String url;
	Set<Tag> tags;

	public SolrHydratedWebsite(Integer id, String headline, String description, String url) {
		this.id = id;
		this.headline = headline;
		this.description = description;
		this.url = url;
		this.tags = new ListOrderedSet();
	}

	@Override
	public void addTag(Tag tag) {
		tags.add(tag);
		
	}

	@Override
	public void clearTags() {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Set<DiscoveredFeed> getDiscoveredFeeds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getEmbargoedUntil() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Geocode getGeocode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getHttpStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Date getLastChanged() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getLastScanned() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getLiveTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return headline;
	}

	@Override
	public User getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getRemoveTag(Tag tag) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Set<Tag> getTags() {
		return tags;
	}
	
	@Override
	public String getType() {
		return "N";
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public String getUrlWords() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isHeld() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDate(Date date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDiscoveredFeeds(Set<DiscoveredFeed> discoveredFeeds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEmbargoedUntil(Date embargoedUntil) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGeocode(Geocode geocode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHeld(boolean held) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHttpStatus(int httpStatus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setId(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLastChanged(Date lastChanged) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLastScanned(Date lastScanned) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLiveTime(Date time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOwner(User owner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTags(Set<Tag> tags) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUrl(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUrlWords(String urlWords) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public Set<CalendarFeed> getCalendars() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Feed> getFeeds() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Set<Watchlist> getWatchlist() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCalendars(Set<CalendarFeed> calendars) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Date getDate() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
