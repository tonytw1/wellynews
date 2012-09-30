package nz.co.searchwellington.model;

import nz.co.searchwellington.repositories.ConfigDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SiteInformation {

	private ConfigDAO configDAO;
	
    @Value("#{config['areaname']}")
	private String areaname;

    @Value("#{config['installed']}")
	private String url;
    
    @Value("#{config['twitter.username']}")
    private String twitterUsername;
    
    @Value("#{config['googlemaps.apikey']}")
    private String googleMapsApiKey;
    
    @Value("#{config['imageroot']}")  
    private String imageRoot;
    
    @Value("#{config['staticroot']}")
    private String staticRoot;
    
    @Value("#{config['feedburner.url']}")
    private String feedburnerUrl;
    
    public SiteInformation() {
	}
    
    @Autowired
	public SiteInformation(ConfigDAO configDAO) {	
		this.configDAO = configDAO;
	}
    
    public String getAreaname() {
		return areaname;
	}
    
	public String getSitename() {
	    return "Search " + this.areaname;
	}
	
	public String getTagline() {
		return areaname + " in a box";
	}
	
	public String getUrl() {
		return url;
	}
	
    public String getTwitterUsername() {
        return twitterUsername;
    }
    
    public String getGoogleMapsApiKey() {
        return googleMapsApiKey;
    }


    public String getTrackingCode() {
    	return configDAO.getConfig().getStatsTracking();
    }

	public String getStaticRoot() {
		return staticRoot;
	}

	public String getFeedburnerUrl() {
		return feedburnerUrl;
	}

	public String getImageRoot() {
		return imageRoot;
	}

	public boolean isTwitterEnabled() {
		return twitterUsername != null && !twitterUsername.isEmpty();
	}
		
}
