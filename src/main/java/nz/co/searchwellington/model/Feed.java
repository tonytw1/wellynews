package nz.co.searchwellington.model;

import java.util.Date;

public interface Feed extends PublishedResource {	// TODO persisted classses shouldn't need to implement frontend interfaces

    public FeedAcceptancePolicy getAcceptancePolicy();

    public void setAcceptancePolicy(FeedAcceptancePolicy acceptancePolicy);

    public void setLatestItemDate(Date latestPublicationDate);

    public Date getLatestItemDate();

    public Date getLastRead();

    public void setLastRead(Date lastRead);
        
    public String getUrlWords();

	public String getWhakaokoId();

	public void setWhakaokoId(String whakaokoId);
	
}
