package nz.co.searchwellington.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;
import uk.co.eelpieconsulting.common.views.rss.RssFeedable;

import nz.co.searchwellington.model.frontend.FrontendImage;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;

import com.google.common.collect.ImmutableList;

public class NewsitemImpl extends PublishedResourceImpl implements Newsitem, FrontendNewsitem, RssFeedable {
	
	private static final long serialVersionUID = 1L;
	
	CommentFeed commentFeed;	// TODO should be visible on the interface
    Image image;
    Feed feed;
    Date accepted;
    User acceptedBy;
    
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
	
	public Set<Twit> getReTwits() {
		return reTwits;
	}
	
	@Override
	public List<Twit> getRetweets() {
		return ImmutableList.<Twit>builder().addAll(reTwits).build();
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

	public User getAcceptedBy() {
		return acceptedBy;
	}

	public void setAcceptedBy(User acceptedBy) {
		this.acceptedBy = acceptedBy;
	}

	@Override
	public String getAcceptedFromFeedName() {
		if (feed != null) {
			return feed.getName();
		}
		return null;
	}

	@Override
	public String getAcceptedByProfilename() {
		if (acceptedBy != null) {
			return acceptedBy.getProfilename();
		}
		return null;
	}

	@Override
	public Place getPlace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPlace(Place place) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FrontendImage getFrontendImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeadline() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getImageUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LatLong getLatLong() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWebUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	
}