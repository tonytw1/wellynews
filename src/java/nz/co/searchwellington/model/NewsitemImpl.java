package nz.co.searchwellington.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.W3CGeoModuleImpl;
import com.sun.syndication.feed.synd.SyndEntry;


public class NewsitemImpl extends PublishedResourceImpl implements Newsitem {
    
        
    CommentFeed commentFeed;
    private String twitterSubmitter;
    private String twitterMessage;
    
    public NewsitemImpl() {}
    
    public NewsitemImpl(int id, String name, String url, String description, Date date, Website publisher, Set <Tag> tags, Set<DiscoveredFeed> discoveredFeeds) {       
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.date = date;
        this.publisher = publisher;
        this.tags = tags;
        this.discoveredFeeds = discoveredFeeds;
        this.twitterSubmitter = null;
    }

 
    public String getType() {
        return "N";
    }
    
         
    @SuppressWarnings("unchecked")
	@Override
    public SyndEntry getRssItem() {
        SyndEntry rssItem = super.getRssItem();
        rssItem.setPublishedDate(this.getDate());        
        final Geocode geocode = this.getGeocode();
        if (geocode != null && geocode.isValid()) {            
        	GeoRSSModule geoRSSModule = new W3CGeoModuleImpl();     
        	geoRSSModule.setLatitude(geocode.getLatitude());
        	geoRSSModule.setLongitude(geocode.getLongitude());
        	rssItem.getModules().add(geoRSSModule);            
        }
        return rssItem;
    }
    
    
   
    public List<Comment> getComments() {
    	List<Comment> comments = new ArrayList<Comment>();
    	if (getCommentFeed() != null) {
    		return getCommentFeed().getComments();    		
    	}
    	return comments;
	}

	public CommentFeed getCommentFeed() {
        return commentFeed;
    }

    public void setCommentFeed(CommentFeed commentFeed) {
        this.commentFeed = commentFeed;
    }

    public String getTwitterSubmitter() {
        return twitterSubmitter;
    }

    public void setTwitterSubmitter(String twitterSubmitter) {
        this.twitterSubmitter = twitterSubmitter;
    }

    public String getTwitterMessage() {
        return twitterMessage;
    }

    public void setTwitterMessage(String twitterMessage) {
        this.twitterMessage = twitterMessage;
    }


    
    
    
}
