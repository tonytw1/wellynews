package nz.co.searchwellington.model;

import java.util.Date;

public class FeedImpl extends PublishedResourceImpl implements Feed {
	
	private static final long serialVersionUID = 1L;
	
	// TODO migrate acceptance options to Enums.
    String acceptancePolicy;
    Date latestItemDate;
    Date lastRead;
    String whakaokoId;
    
    public FeedImpl() {
    }
        
    public FeedImpl(int id, String name, String url, String description, Website publisher, String acceptancePolicy) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.description = description;
        this.publisher = publisher;
        this.acceptancePolicy = acceptancePolicy;
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

    @Override
	public String getWhakaokoId() {
		return whakaokoId;
	}

    @Override
	public void setWhakaokoId(String whakaokoId) {
		this.whakaokoId = whakaokoId;
	}

	@Override
	public String toString() {
		return "FeedImpl [acceptancePolicy=" + acceptancePolicy + ", lastRead="
				+ lastRead + ", latestItemDate=" + latestItemDate
				+ ", whakaokoId=" + whakaokoId + "]";
	}
        
}
