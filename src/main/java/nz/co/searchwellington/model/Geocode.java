package nz.co.searchwellington.model;

import geo.google.datamodel.GeoAltitude;
import geo.google.datamodel.GeoCoordinate;
import geo.google.datamodel.GeoUtils;

import org.apache.log4j.Logger;

public class Geocode {
    
    private static Logger log = Logger.getLogger(Geocode.class);
        
    private int id;
    private String address;
    private double latitude;
    private double longitude;
    private String type;
    
    public Geocode() {        
    }
            
    public Geocode(String geocode) {
        this.address = geocode;
        this.latitude = 0;
        this.longitude = 0;          
    }
        
    public Geocode(String address, double latitude, double longitude) {     
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Geocode(Double latitude, Double longitude) {
    	 this.latitude = latitude;
         this.longitude = longitude;
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
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double d) {
        this.latitude = d;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public boolean isValid() {
        return latitude != 0 && longitude != 0;	// TODO Should be nullable
    }
    
    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	// TODO These two compare methods shouldn't really be on the domain model
    public boolean isSameLocation(Geocode other) {        
        final double distanceBetweenInKm = getDistanceTo(other.getLatitude(), other.getLongitude());
        log.debug("Points " + this.getAddress() + " and " + other.getAddress() + " are " + distanceBetweenInKm + " km part");
        return distanceBetweenInKm < 0.1;
    }
    
	public double getDistanceTo(double otherLatitude, double otherLongitude) {
		GeoCoordinate thisPoint = new GeoCoordinate(this.getLatitude(), this.getLongitude(), new GeoAltitude(0));
		GeoCoordinate otherPoint = new GeoCoordinate(otherLatitude, otherLongitude, new GeoAltitude(0));
		double distanceBetweenInKm = GeoUtils.distanceBetweenInKm(thisPoint, otherPoint);
		log.debug("Distance to " + latitude + ", " + longitude + " is " + distanceBetweenInKm);
		return distanceBetweenInKm;
	}
	
}
