package nz.co.searchwellington.model.decoraters.highlighting;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;

import com.sun.syndication.feed.synd.SyndEntry;

public class SolrHighlightingNewsitemDecorator implements Newsitem {
	
	private Newsitem newsitem;
	private Map<String, List<String>> map;
   
   
    public SolrHighlightingNewsitemDecorator(Newsitem newsitem, Map<String, List<String>> map) {
    	this.newsitem = newsitem;
    	this.map = map;
    }
    
     
    public void addTag(Tag tag) {
		newsitem.addTag(tag);
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


	public Geocode getGeocode() {
		return newsitem.getGeocode();
	}


	public int getHttpStatus() {
		return newsitem.getHttpStatus();
	}


	public int getId() {
		return newsitem.getId();
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
		if (map.containsKey("name")) {
			return map.get("name").get(0);
		}
		return newsitem.getName();
	}


	public Website getPublisher() {
		return newsitem.getPublisher();
	}


	public void getRemoveTag(Tag tag) {
		newsitem.getRemoveTag(tag);
	}


	public SyndEntry getRssItem() {
		return newsitem.getRssItem();
	}


	public Set<Tag> getTags() {
		return newsitem.getTags();
	}


	public int getTechnoratiCount() {
		return newsitem.getTechnoratiCount();
	}


	public String getTwitterMessage() {
		return newsitem.getTwitterMessage();
	}


	public String getTwitterSubmitter() {
		return newsitem.getTwitterSubmitter();
	}


	public String getType() {
		return newsitem.getType();
	}


	public String getUrl() {
		return newsitem.getUrl();
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


	public void setGeocode(Geocode geocode) {
		newsitem.setGeocode(geocode);
	}


	public void setHttpStatus(int httpStatus) {
		newsitem.setHttpStatus(httpStatus);
	}


	public void setId(int id) {
		newsitem.setId(id);
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


	public void setPublisher(Website publisher) {
		newsitem.setPublisher(publisher);
	}


	public void setTags(Set<Tag> tags) {
		newsitem.setTags(tags);
	}


	public void setTechnoratiCount(int technoratiCount) {
		newsitem.setTechnoratiCount(technoratiCount);
	}

	public Long getTwitterId() {
		return newsitem.getTwitterId();
	}


	public void setTwitterId(Long twitterId) {
		newsitem.setTwitterId(twitterId);
	}


	public void setTwitterMessage(String twitterMessage) {
		newsitem.setTwitterMessage(twitterMessage);
	}


	public void setTwitterSubmitter(String submitter) {
		newsitem.setTwitterSubmitter(submitter);
	}


	public void setUrl(String url) {
		newsitem.setUrl(url);
	}


	public String getUrlWords() {
		return newsitem.getUrlWords();
	}


	public void setUrlWords(String urlWords) {
		newsitem.setUrlWords(urlWords);
	}


	public User getOwner() {
		return newsitem.getOwner();
	}


	public void setOwner(User owner) {
		newsitem.setOwner(owner);		
	}
	
}
