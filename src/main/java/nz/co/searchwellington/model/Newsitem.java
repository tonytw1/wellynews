package nz.co.searchwellington.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface Newsitem extends PublishedResource, Commentable {

	public Image getImage();

	public void setImage(Image image);
	
	public Set<Twit> getReTwits();
	public void addReTwit(Twit retwit);
	public List<Twit> getRetweets();	// TODO what's the difference with getReTwits()?	// TODO it's an implementation detail - the hiberate collection is a Set, the interface wants a List
	
	public Feed getFeed();
	public void setFeed(Feed feed);

	public Date getAccepted();

	public void setAccepted(Date accepted);

	public User getAcceptedBy();
	public void setAcceptedBy(User user);

		
}
