package nz.co.searchwellington.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Geocode implements Serializable {
    
	private static final long serialVersionUID = 1L;
	
    private int id;
    private String address;
    private Double latitude;
    private Double longitude;
    private String type;
    private Long osmId;
    private String osmType;
    private String resolver;
    
    public Geocode() {        
    }
            
    public Geocode(Double latitude, Double longitude) {
    	this.latitude = latitude;
    	this.longitude = longitude;
    }
    
    public Geocode(String address, Double latitude, Double longitude) {     
    	this.address = address;
    	this.latitude = latitude;
    	this.longitude = longitude;
    }
    
    public Geocode(String address, double latitude, double longitude, Long osmId, String osmType) {
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.osmId = osmId;
		this.osmType = osmType;
	}

	public Geocode(String address, double latitude, double longitude, String type, Long osmId, String osmType, String resolver) {
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.type = type;
		this.osmId = osmId;
		this.osmType = osmType;
		this.resolver = resolver;
	}
	
	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double d) {
        this.latitude = d;
    }
    public Double getLongitude() {
    	return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public boolean isValid() {
        return latitude != null && longitude != null;
    }
    
    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Long getOsmId() {
		return osmId;
	}

	public void setOsmId(Long osmId) {
		this.osmId = osmId;
	}
	
	public String getOsmType() {
		return osmType;
	}
	
	public void setOsmType(String osmType) {
		this.osmType = osmType;
	}
	
	@Deprecated
	public String getOsmPlaceId() {
		if (osmId != null && osmType != null) {
			return osmId + "/" + osmType.substring(0, 1).toUpperCase();
		}
		return null;
	}
	
	public String getResolver() {
		return resolver;
	}
	
	public void setResolver(String resolver) {
		this.resolver = resolver;
	}
	
	@Override
	public String toString() {
		return "Geocode [address=" + address + ", id=" + id + ", latitude="
				+ latitude + ", longitude=" + longitude + ", osmPlaceId="
				+ osmId + ", type=" + type + "]";
	}
	
	public String getDisplayName() {
		if (address !=  null) {
			return address;
		}
		return latitude + ", " + longitude;		
	}
	
}
