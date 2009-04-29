package nz.co.searchwellington.config;

public interface Config {

    public int getId();
    public void setId(int id);
    
    public String getStatsTracking();
    public void setStatsTracking(String statsTracking);
    public String getFlickrPoolGroupId();
    public void setFlickrPoolGroupId(String flickrPoolGroupId);
    public String getUseClickthroughCounter();
    public void setUseClickthroughCounter(String useClickthroughCounter);
    public boolean isTwitterListenerEnabled();
	public void setTwitterListenerEnabled(boolean twitterListenerEnabled);
  
}