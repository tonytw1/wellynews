package nz.co.searchwellington.model;

import java.util.Set;

public interface Newsitem extends PublishedResource, TwitterSubmittable, Commentable {

	public Image getImage();

	public void setImage(Image image);
	
	public Set<Twit> getReTwits();
	public void addReTwit(Twit retwit);
	
	public Feed getFeed();
	public void setFeed(Feed feed);
	
}
