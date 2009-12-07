package nz.co.searchwellington.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.sun.syndication.feed.module.georss.GeoRSSModule;
import com.sun.syndication.feed.module.georss.W3CGeoModuleImpl;
import com.sun.syndication.feed.synd.SyndEntry;


public class NewsitemImpl extends PublishedResourceImpl implements Newsitem {
         
    CommentFeed commentFeed;	// TODO should be visible on the interface
    Image image;
    Feed feed;
      
    private Twit submittingTwit;
    private Set<Twit> reTwits;
    
    
    public NewsitemImpl() {
    }
    
    public NewsitemImpl(int id, String name, String url, String description, Date date, Website publisher, Set <Tag> tags, Set<DiscoveredFeed> discoveredFeeds, Twit submittingTwit,  Set<Twit> retwits) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.date = date;
        this.publisher = publisher;
        this.tags = tags;
        this.discoveredFeeds = discoveredFeeds;
        this.submittingTwit = submittingTwit;
        this.reTwits = retwits;
        this.feed = null;
    }

 
    public String getType() {
        return "N";
    }
    
         
    @SuppressWarnings("unchecked")
	@Override
    public SyndEntry getRssItem() {
        SyndEntry rssItem = super.getRssItem();
        rssItem.setPublishedDate(this.getDate());
        if (this.getPublisher() != null) {
        	rssItem.setAuthor(this.getPublisher().getName());
        }
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

    

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	
	public Twit getSubmittingTwit() {
		return submittingTwit;
	}

	public void setSubmittingTwit(Twit submittingTwit) {
		this.submittingTwit = submittingTwit;
	}

	public Set<Twit> getReTwits() {
		return reTwits;
	}

	public void addReTwit(Twit retwit) {
		this.reTwits.add(retwit);
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}
	  
}