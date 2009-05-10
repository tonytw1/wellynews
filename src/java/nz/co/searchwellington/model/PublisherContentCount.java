package nz.co.searchwellington.model;

public class PublisherContentCount {

	private Website publisher;
	private long count;
	
	public PublisherContentCount(Website publisher, long count) {		
		this.publisher = publisher;
		this.count = count;
	}
	
	public Website getPublisher() {
		return publisher;
	}
	
	public void setPublisher(Website publisher) {
		this.publisher = publisher;
	}
	
	public long getCount() {
		return count;
	}
	
	public void setCount(long count) {
		this.count = count;
	}
	
}
