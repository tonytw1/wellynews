package nz.co.searchwellington.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class NewsitemImpl extends PublishedResourceImpl implements Newsitem {
         
    CommentFeed commentFeed;	// TODO should be visible on the interface
    Image image;
    Feed feed;
    Date accepted;
    
    private Set<Twit> reTwits;
    
    
    public NewsitemImpl() {
    }
    
    public NewsitemImpl(int id, String name, String url, String description, Date date, Website publisher, Set<DiscoveredFeed> discoveredFeeds, Twit submittingTwit,  Set<Twit> retwits) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.date = date;
        this.publisher = publisher;
        this.discoveredFeeds = discoveredFeeds;
        this.reTwits = retwits;
        this.feed = null;
    }

 
    public String getType() {
        return "N";
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

	public Date getAccepted() {
		return accepted;
	}

	public void setAccepted(Date accepted) {
		this.accepted = accepted;
	}
	
}