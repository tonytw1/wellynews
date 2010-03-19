package nz.co.searchwellington.model;

import nz.co.searchwellington.repositories.ConfigRepository;

public class SiteInformation {

	private ConfigRepository configDAO;
	private String areaname;
	private String url;
    private String adsenseAccount;
    private String twitterUsername;
    private String googleMapsApiKey;
    private String imageRoot;
    private String staticRoot;
    private String feedburnerUrl;
    
	public SiteInformation(ConfigRepository configDAO) {	
		this.configDAO = configDAO;
	}

	public String getAdsenseAccount() {
        return adsenseAccount;
    }

    public void setAdsenseAccount(String adsenseAccount) {
        this.adsenseAccount = adsenseAccount;
    }
    
    public String getAreaname() {
		return areaname;
	}

	public void setAreaname(String areaname) {
		this.areaname = areaname;
	}

	public String getSitename() {
	    return "Search " + this.areaname;
	}
    
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


    public String getTwitterUsername() {
        return twitterUsername;
    }


    public void setTwitterUsername(String twitterUsername) {
        this.twitterUsername = twitterUsername;
    }


    public String getGoogleMapsApiKey() {
        return googleMapsApiKey;
    }


    public void setGoogleMapsApiKey(String googleMapsApiKey) {
        this.googleMapsApiKey = googleMapsApiKey;
    }

    public String getTrackingCode() {
    	return configDAO.getConfig().getStatsTracking();
    }

	public String getStaticRoot() {
		return staticRoot;
	}

	public void setStaticRoot(String staticRoot) {
		this.staticRoot = staticRoot;
	}

	public String getFeedburnerUrl() {
		return feedburnerUrl;
	}

	public void setFeedburnerUrl(String feedburnerUrl) {
		this.feedburnerUrl = feedburnerUrl;
	}

	public String getImageRoot() {
		return imageRoot;
	}

	public void setImageRoot(String imageRoot) {
		this.imageRoot = imageRoot;
	}

	public boolean isTwitterEnabled() {
		return twitterUsername != null && !twitterUsername.isEmpty();
	}
		
}
