package nz.co.searchwellington.model.decoraters;

import java.util.Date;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;

public class FeedWrapper extends ResourceWrapper implements Feed {
    
    private SiteInformation siteInformation;
    Feed resource;
    private boolean showAdmin;
    
    public FeedWrapper(Feed resource, SiteInformation siteInformation, User loggedInUser) {
        super(resource);
        this.siteInformation = siteInformation;
        this.resource = resource;
        
        showAdmin = false;
        if (loggedInUser != null) {
            this.showAdmin = true;
        }
    }

    @Override
    public String getUrl() {
        return siteInformation.getUrl() + "/viewfeed?feed=" + getId();
    }

       
    public Date getLatestItemDate() {
        return resource.getLatestItemDate();
    }

    public void setAcceptancePolicy(String acceptancePolicy) {       
    }

    public void setLatestItemDate(Date latestPublicationDate) {       
    }

 
    public String getAcceptancePolicy() {
        if (showAdmin) {
            return resource.getAcceptancePolicy();
        }
        return null;
    }

    public Date getLastRead() {
        if (showAdmin) {
            return resource.getLastRead();
        }
        return null;
    }

    public void setLastRead(Date lastRead) {     
    }

    public Website getPublisher() {
        return resource.getPublisher();
    }

    public void setPublisher(Website publisher) {            
    }

	public String getUrlWords() {
		return resource.getUrlWords();
	}

    
}
