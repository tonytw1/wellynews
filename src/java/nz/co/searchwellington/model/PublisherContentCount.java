package nz.co.searchwellington.model;

public class PublisherContentCount implements Comparable {

	private Website publisher;
	private int count;
	
	public PublisherContentCount(Website publisher, int count) {		
		this.publisher = publisher;
		this.count = count;
	}
	
	public Website getPublisher() {
		return publisher;
	}
	
	public void setPublisher(Website publisher) {
		this.publisher = publisher;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public int compareTo(Object event) {       
        if (event instanceof PublisherContentCount) {
            if (this.publisher == null) {                
                return -1;
            }
            return this.publisher.getName().compareTo(((PublisherContentCount) event).getPublisher().getName());
        }        
        return 0;
    }
	
}
