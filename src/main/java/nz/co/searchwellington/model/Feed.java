package nz.co.searchwellington.model;

import java.util.Date;


public interface Feed extends PublishedResource {

    public String getAcceptancePolicy();

    public void setAcceptancePolicy(String acceptancePolicy);

    public void setLatestItemDate(Date latestPublicationDate);

    public Date getLatestItemDate();

    public Date getLastRead();

    public void setLastRead(Date lastRead);
        
    public String getUrlWords();
	
}
