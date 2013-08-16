package nz.co.searchwellington.config;

@Deprecated	//TODO No need for runtime config now that environment specific properties files are working nicely.
public class Config {
    
    private int id;  
    private String statsTracking;
    private String flickrPoolGroupId;
    private String useClickthroughCounter;
    private boolean twitterListenerEnabled;
	private boolean feedReadingEnabled;
    
    public Config() {
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
	
	public boolean isFeedReadingEnabled() {
		return feedReadingEnabled;
	}

	public void setFeedReadingEnabled(boolean feedReadingEnabled) {
		this.feedReadingEnabled = feedReadingEnabled;
	}
	
}