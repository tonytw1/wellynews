package nz.co.searchwellington.config;

public class ConfigImpl implements Config {
    
    int id;  
    String statsTracking;
    String flickrPoolGroupId;
    String useClickthroughCounter;
    private boolean twitterListenerEnabled;
    
    public ConfigImpl() {
    }
    
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public String getStatsTracking() {
        return statsTracking;
    }


    public void setStatsTracking(String statsTracking) {
        this.statsTracking = statsTracking;
    }


    public String getFlickrPoolGroupId() {
        return flickrPoolGroupId;
    }


    public void setFlickrPoolGroupId(String flickrPoolGroupOId) {
        this.flickrPoolGroupId = flickrPoolGroupOId;
    }


    public String getUseClickthroughCounter() {
        return useClickthroughCounter;
    }


    public void setUseClickthroughCounter(String useClickthroughCounter) {
        this.useClickthroughCounter = useClickthroughCounter;
    }


	public void setTwitterListenerEnabled(boolean twitterListenerEnabled) {
		this.twitterListenerEnabled = twitterListenerEnabled;
	}


	public boolean isTwitterListenerEnabled() {
		return twitterListenerEnabled;
	}

}
