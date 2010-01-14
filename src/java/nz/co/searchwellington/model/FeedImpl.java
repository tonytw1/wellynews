package nz.co.searchwellington.model;

import java.util.Date;
import java.util.Set;



public class FeedImpl extends PublishedResourceImpl implements Feed {
  
    // TODO migrate acceptance options to Enums.
    String acceptancePolicy;
    Date latestItemDate;
    Date lastRead;
    
    public FeedImpl() {
    }
    
    
    public FeedImpl(int id, String name, String url, String description, Website publisher, String acceptancePolicy, Set<Tag> tags) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.publisher = publisher;
        this.acceptancePolicy = acceptancePolicy;
        this.tags = tags;
    }
    
    
  
    public String getType() {
        return "F";
     }

   

    public String getAcceptancePolicy() {
        return acceptancePolicy;
    }


    public void setAcceptancePolicy(String acceptancePolicy) {
        this.acceptancePolicy = acceptancePolicy;
    }
    
    
    public Date getLatestItemDate() {
        return latestItemDate;
    }

    public void setLatestItemDate(Date latestPublicationDate) {
        this.latestItemDate = latestPublicationDate;      
    }

    public Date getLastRead() {
        return lastRead;
    }

    public void setLastRead(Date lastRead) {
        this.lastRead = lastRead;
    }
    
    public boolean isScreenScraped() {
    	return url.startsWith("http://brownbag.wellington.gen.nz/");    	
    }
    
        
}
