package nz.co.searchwellington.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

public class NewsitemImpl extends PublishedResourceImpl implements Newsitem {
	
	private static final long serialVersionUID = 1L;
	
	CommentFeed commentFeed;	// TODO should be visible on the interface
    Image image;
    Feed feed;
    Date accepted;
    User acceptedBy;

    public NewsitemImpl() {
    }
    
    public NewsitemImpl(int id, String name, String url, String description, Date date, Website publisher, Set<DiscoveredFeed> discoveredFeeds) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.date = date;
        this.publisher = publisher;
        this.discoveredFeeds = discoveredFeeds;
        this.feed = null;
    }
    
    public String getType() {
        return "N";
    }
    
    public List<Comment> getComments() {
    	if (getCommentFeed() != null) {
    		return ImmutableList.<Comment>builder().addAll(getCommentFeed().getComments()).build();
    	}
    	return Collections.emptyList();
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

	public User getAcceptedBy() {
		return acceptedBy;
	}

	public void setAcceptedBy(User acceptedBy) {
		this.acceptedBy = acceptedBy;
	}
	
}