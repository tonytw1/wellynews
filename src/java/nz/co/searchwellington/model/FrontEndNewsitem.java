package nz.co.searchwellington.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class FrontEndNewsitem implements Newsitem {
	
	Newsitem newsitem;
	private List<Tag> tags;

	public FrontEndNewsitem(Newsitem newsitem) {		
		this.newsitem = newsitem;
		tags = new ArrayList<Tag>();
	}

	public List<Tag> getTags() {
		return tags;
	}

	
	public void addTag(Tag tag) {
		tags.add(tag);
	}

	
	public void addReTwit(Twit retwit) {
		newsitem.addReTwit(retwit);
	}

	public CommentFeed getCommentFeed() {
		return newsitem.getCommentFeed();
	}

	public List<Comment> getComments() {
		return newsitem.getComments();
	}

	public Date getDate() {
		return newsitem.getDate();
	}

	public String getDescription() {
		return newsitem.getDescription();
	}

	public Set<DiscoveredFeed> getDiscoveredFeeds() {
		return newsitem.getDiscoveredFeeds();
	}

	public Date getEmbargoedUntil() {
		return newsitem.getEmbargoedUntil();
	}

	public Feed getFeed() {
		return newsitem.getFeed();
	}

	public Geocode getGeocode() {
		return newsitem.getGeocode();
	}

	public int getHttpStatus() {
		return newsitem.getHttpStatus();
	}

	public int getId() {
		return newsitem.getId();
	}

	public Image getImage() {
		return newsitem.getImage();
	}

	public Date getLastChanged() {
		return newsitem.getLastChanged();
	}

	public Date getLastScanned() {
		return newsitem.getLastScanned();
	}

	public Date getLiveTime() {
		return newsitem.getLiveTime();
	}

	public String getName() {
		return newsitem.getName();
	}

	public User getOwner() {
		return newsitem.getOwner();
	}

	public Website getPublisher() {
		return newsitem.getPublisher();
	}

	public String getPublisherName() {
		return newsitem.getPublisherName();
	}

	public Set<Twit> getReTwits() {
		return newsitem.getReTwits();
	}

	public Twit getSubmittingTwit() {
		return newsitem.getSubmittingTwit();
	}

	public String getType() {
		return newsitem.getType();
	}

	public String getUrl() {
		return newsitem.getUrl();
	}

	public String getUrlWords() {
		return newsitem.getUrlWords();
	}

	public boolean isHeld() {
		return newsitem.isHeld();
	}

	public void setCommentFeed(CommentFeed commentFeed) {
		newsitem.setCommentFeed(commentFeed);
	}

	public void setDate(Date date) {
		newsitem.setDate(date);
	}

	public void setDescription(String description) {
		newsitem.setDescription(description);
	}

	public void setDiscoveredFeeds(Set<DiscoveredFeed> discoveredFeeds) {
		newsitem.setDiscoveredFeeds(discoveredFeeds);
	}

	public void setEmbargoedUntil(Date embargoedUntil) {
		newsitem.setEmbargoedUntil(embargoedUntil);
	}

	public void setFeed(Feed feed) {
		newsitem.setFeed(feed);
	}

	public void setGeocode(Geocode geocode) {
		newsitem.setGeocode(geocode);
	}

	public void setHeld(boolean held) {
		newsitem.setHeld(held);
	}

	public void setHttpStatus(int httpStatus) {
		newsitem.setHttpStatus(httpStatus);
	}

	public void setId(int id) {
		newsitem.setId(id);
	}

	public void setImage(Image image) {
		newsitem.setImage(image);
	}

	public void setLastChanged(Date lastChanged) {
		newsitem.setLastChanged(lastChanged);
	}

	public void setLastScanned(Date lastScanned) {
		newsitem.setLastScanned(lastScanned);
	}

	public void setLiveTime(Date time) {
		newsitem.setLiveTime(time);
	}

	public void setName(String name) {
		newsitem.setName(name);
	}

	public void setOwner(User owner) {
		newsitem.setOwner(owner);
	}

	public void setPublisher(Website publisher) {
		newsitem.setPublisher(publisher);
	}

	public void setSubmittingTwit(Twit submittingTwit) {
		newsitem.setSubmittingTwit(submittingTwit);
	}

	public void setUrl(String url) {
		newsitem.setUrl(url);
	}

	public void setUrlWords(String urlWords) {
		newsitem.setUrlWords(urlWords);
	}
	
}