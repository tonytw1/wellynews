package nz.co.searchwellington.model.decoraters.highlighting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

public class SolrHighlightingNewsitemDecorator implements Newsitem {
	
	private Newsitem newsitem;
	private Map<String, List<String>> map;
   
   
    public SolrHighlightingNewsitemDecorator(Newsitem newsitem, Map<String, List<String>> map) {
    	this.newsitem = newsitem;
    	this.map = map;
    }
       
    public String getName() {
		if (map.containsKey("name")) {
			return map.get("name").get(0);
		}
		return newsitem.getName();
	}
    
    public String getDescription() {    	
    	if (map.containsKey("description")) {
			return map.get("description").get(0);
		}
		return newsitem.getDescription();
	}
    
    
    
    public String getPublisherName() {
		return newsitem.getPublisherName();
	}

	public List<String> getBodytext() {    	
    	if (map.containsKey("bodytext")) {
			return map.get("bodytext");
		}
		return null;
	}
    
    
    public void addTag(Tag tag) {
		newsitem.addTag(tag);
	}

	public CommentFeed getCommentFeed() {
		return newsitem.getCommentFeed();
	}
	
	public void clearTags() {
		newsitem.clearTags();		
	}

	public List<Comment> getComments() {		
		List<Comment> highlightedComments = new ArrayList<Comment>();		
    	if (map.containsKey("comment")) {
    		List<String> list = map.get("comment");
    		for (String string : list) {
    			Comment highlightedComment = new Comment();
    			highlightedComment.setTitle(string);
    			highlightedComments.add(highlightedComment);
			}
    	}
		return highlightedComments;
	}


	public Date getDate() {
		return newsitem.getDate();
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

	
	
	public Date getEmbargoedUntil() {
		return newsitem.getEmbargoedUntil();
	}

	public void setEmbargoedUntil(Date embargoedUntil) {
		newsitem.setEmbargoedUntil(embargoedUntil);
	}

	public Website getPublisher() {
		return newsitem.getPublisher();
	}


	public void getRemoveTag(Tag tag) {
		newsitem.getRemoveTag(tag);
	}

	public Set<Tag> getTags() {
		return newsitem.getTags();
	}


	public int getTechnoratiCount() {
		return newsitem.getTechnoratiCount();
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


	public void setImage(Image image) {
	}


	public Image getImage() {
		return null;
	}


	
	
	public Twit getSubmittingTwit() {
		return newsitem.getSubmittingTwit();
	}


	public void setSubmittingTwit(Twit submittingTwit) {
		newsitem.setSubmittingTwit(submittingTwit);
	}


	public void addReTwit(Twit retwit) {
		newsitem.addReTwit(retwit);
	}


	public Set<Twit> getReTwits() {
		return newsitem.getReTwits();
	}


	public Feed getFeed() {
		return newsitem.getFeed();
	}


	public void setFeed(Feed feed) {
		newsitem.setFeed(feed);
	}

	public boolean isHeld() {
		return newsitem.isHeld();
	}

	public void setHeld(boolean held) {
		newsitem.setHeld(held);
	}

	
	
		
}
