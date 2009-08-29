package nz.co.searchwellington.model;

public interface Newsitem extends PublishedResource, TwitterSubmittable, Commentable {

	public Image getImage();

	public void setImage(Image image);
   
}
