package nz.co.searchwellington.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SiteInformation {
	
    @Value("${areaname}")
	private String areaname;

    @Value("${installed}")
	private String url;
    
    @Value("${twitter.username}")
    private String twitterUsername;

    @Value("${imageroot}")
    private String imageRoot;
    
    @Value("${staticroot}")
    private String staticRoot;
    
    public SiteInformation() {
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

	public String getStaticRoot() {
		return staticRoot;
	}
	
	public String getImageRoot() {
		return imageRoot;
	}

	public boolean isTwitterEnabled() {
		return twitterUsername != null && !twitterUsername.isEmpty();
	}
		
}
