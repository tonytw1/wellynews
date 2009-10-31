package nz.co.searchwellington.model;

import java.util.Date;
import java.util.Set;


public interface Feed extends PublishedResource {

    public String getAcceptancePolicy();

    public void setAcceptancePolicy(String acceptancePolicy);

    public void setLatestItemDate(Date latestPublicationDate);

    public Date getLatestItemDate();

    public Date getLastRead();

    public void setLastRead(Date lastRead);
        
    public String getUrlWords();

	public Set <Newsitem> getNewsitems();

	public void setNewsitems(Set<Newsitem> newsitems);
}
