package nz.co.searchwellington.model;

import java.util.Date;
import java.util.Set;

public interface Newsitem extends PublishedResource, Commentable {

	public Image getImage();

	public void setImage(Image image);
	
	public Set<Twit> getReTwits();
	public void addReTwit(Twit retwit);
	
	public Feed getFeed();
	public void setFeed(Feed feed);

	public Date getAccepted();

	public void setAccepted(Date accepted);

	public void setAcceptedBy(User user);
	
}
