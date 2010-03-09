package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;
import java.util.Set;

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
import nz.co.searchwellington.model.Website;

import org.apache.commons.collections.set.ListOrderedSet;

public class SolrHydratedNewsitem implements Newsitem {
	
	int id;
	String headline;
	String description;
	String url;
	String publisherName;
	Date date;

	public SolrHydratedNewsitem(Integer id, String headline, String description, String url, String publisherName, Date date) {
		this.id = id;
		this.headline = headline;
		this.description = description;
		this.url = url;
		this.publisherName = publisherName;
		this.date  = date;		
	}

	@Override
	public void addReTwit(Twit retwit) {
		// TODO Auto-generated method stub		
	}

	
	
	public String getPublisherName() {
		return publisherName;
	}

	@Override
	public Feed getFeed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Twit> getReTwits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFeed(Feed feed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImage(Image image) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Website getPublisher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPublisher(Website publisher) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Date getDate() {
		return date;
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
	public void setUrl(String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUrlWords(String urlWords) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Twit getSubmittingTwit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSubmittingTwit(Twit submittingTwit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CommentFeed getCommentFeed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Comment> getComments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCommentFeed(CommentFeed commentFeed) {
		// TODO Auto-generated method stub
		
	}
	
	

}
