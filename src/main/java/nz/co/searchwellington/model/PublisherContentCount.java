package nz.co.searchwellington.model;

import nz.co.searchwellington.model.frontend.FrontendWebsite;

public class PublisherContentCount implements Comparable {

	private FrontendWebsite publisher;
	private int count;
	
	public PublisherContentCount(FrontendWebsite publisher, int count) {		
		this.publisher = publisher;
		this.count = count;
	}
	
	public FrontendWebsite getPublisher() {
		return publisher;
	}
	
	public void setPublisher(FrontendWebsite publisher) {
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
