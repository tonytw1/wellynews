package nz.co.searchwellington.model;
public class SiteInformation {

	private String areaname;
	private String url;
    private String adsenseAccount;
    private String twitterUsername;
    private String googleMapsApiKey;

    
    // TODO should not need setters.
	public SiteInformation() {		
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


    
    
    
    
    
}
