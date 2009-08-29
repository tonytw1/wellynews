package nz.co.searchwellington.model;

public interface Newsitem extends PublishedResource, TwitterSubmittable, Commentable {

	public String getImageUrl();
	public void setImageUrl(String imageUrl);
   
}
