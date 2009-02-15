package nz.co.searchwellington.model.decoraters.editing;

import java.util.Date;

import nz.co.searchwellington.model.Feed;

public class EditableFeedWrapper extends EditablePublishedResourceWrapper implements Feed {

    Feed resource;
        
	public EditableFeedWrapper(Feed feed) {
	    super(feed);
	    this.resource = feed;
	}
	
    
    public String getDecacheUrl() {
        return "/viewfeed/decache?feed=" + getId();
    }
    
    public String getReadUrl() {
        return "/viewfeed/read?feed=" + getId();
    }
    
    
    public Date getLastRead() {
        return resource.getLastRead();
    }
    
    
    
	public String getAcceptancePolicy() {
		return resource.getAcceptancePolicy();
	}


	
	public Date getLatestItemDate() {
		return resource.getLatestItemDate();
	}

	

	
	public void setAcceptancePolicy(String acceptancePolicy) {
		resource.setAcceptancePolicy(acceptancePolicy);
	}

	

	

	public void setLastRead(Date lastRead) {
		resource.setLastRead(lastRead);
	}

	

	public void setLatestItemDate(Date latestPublicationDate) {
		resource.setLatestItemDate(latestPublicationDate);
	}

}
