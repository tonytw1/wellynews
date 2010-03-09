package nz.co.searchwellington.model;

import java.util.Date;
import java.util.Set;



public abstract class ResourceImpl implements Resource {

    protected int id;
    protected String name;
    protected String url;
    protected int httpStatus;
    
    protected Date date;
    protected String description;
  
    protected Date lastScanned;
    protected Date lastChanged;
    protected Date liveTime;
    
    protected Date embargoedUntil;
    protected boolean held;
    
    protected Snapshot snapshot;

    protected String urlWords;
    
    protected Set<DiscoveredFeed> discoveredFeeds;
      
    protected Geocode geocode;

    protected User owner;
    
    
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
        
    public int getHttpStatus() {
        return httpStatus;
    }
    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }
    
	public Date getLastScanned() {
        return lastScanned;
    }
    public void setLastScanned(Date lastScanned) {
        this.lastScanned = lastScanned;
    }
    
 
    public Date getLastChanged() {
        return lastChanged;
    }
    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }
    
    public Snapshot getSnapshot() {
        return snapshot;
    }
    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }
     
    public Set<DiscoveredFeed> getDiscoveredFeeds() {
        return discoveredFeeds;
    }
    public void setDiscoveredFeeds(Set<DiscoveredFeed> discoveredFeeds) {
        this.discoveredFeeds = discoveredFeeds;
    }
    
    public Date getLiveTime() {
        return liveTime;
    }
    
    public void setLiveTime(Date liveTime) {
        this.liveTime = liveTime;
    }
    
    public Date getEmbargoedUntil() {
		return embargoedUntil;
	}
	public void setEmbargoedUntil(Date embargoedUntil) {
		this.embargoedUntil = embargoedUntil;
	}
  	
	public Geocode getGeocode() {
		return geocode;
	}
	public void setGeocode(Geocode geocode) {
		this.geocode = geocode;
	}
		
	public String getUrlWords() {
		return urlWords;
	}
	public void setUrlWords(String urlWords) {
		this.urlWords = urlWords;
	}
	
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	public boolean isHeld() {
		return held;
	}
	public void setHeld(boolean held) {
		this.held = held;
	}
			
}