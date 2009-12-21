package nz.co.searchwellington.model;

import geo.google.datamodel.GeoAltitude;
import geo.google.datamodel.GeoCoordinate;
import geo.google.datamodel.GeoUtils;

import org.apache.log4j.Logger;

public class Geocode {
    
    private static Logger log = Logger.getLogger(Geocode.class);
    
    
    int id;
    String address;
    double latitude;
    double longitude;
    
    
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
        return latitude != 0 && longitude != 0;
    }
  
    public boolean isSameLocation(Geocode other) {        
        GeoCoordinate thisPoint = new GeoCoordinate(this.getLatitude(), this.getLongitude(), new GeoAltitude(0));
        GeoCoordinate otherPoint = new GeoCoordinate(other.getLatitude(), other.getLongitude(), new GeoAltitude(0));        
        final double distanceBetweenInKm = GeoUtils.distanceBetweenInKm(thisPoint, otherPoint);
        log.debug("Points " + this.getAddress() + " and " + other.getAddress() + " are " + distanceBetweenInKm + " km part");
        return distanceBetweenInKm < 0.1;
    }
    
    
    
    
    
}
