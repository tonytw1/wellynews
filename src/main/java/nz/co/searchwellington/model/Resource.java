package nz.co.searchwellington.model;

import java.util.Date;
import java.util.Set;

public interface Resource {
	
    public int getId();
    public void setId(int id);
    
    public String getName();
    public void setName(String name);
    
    public String getUrl();
    public void setUrl(String url);
  
    public String getDescription();
        
    public String getType();
    
    public Date getDate();
    public void setDate(Date date);
    
    public void setDescription(String description);
    
    public int getHttpStatus();
    public void setHttpStatus(int httpStatus);
    public Date getLastScanned();
    public void setLastScanned(Date lastScanned);
    
    public Date getLastChanged();
    public void setLastChanged(Date lastChanged);
    
    public Set<DiscoveredFeed> getDiscoveredFeeds();
    public void setDiscoveredFeeds(Set<DiscoveredFeed> discoveredFeeds);
    
    public Date getLiveTime();
    public void setLiveTime(Date time);
    
	public Date getEmbargoedUntil();
	public void setEmbargoedUntil(Date embargoedUntil);
    
	public Geocode getGeocode();
	public void setGeocode(Geocode geocode);
	public String getUrlWords();
	public void setUrlWords(String urlWords);
	
	public User getOwner();
	public void setOwner(User owner);

	public boolean isHeld();
	public void setHeld(boolean held);
	
}