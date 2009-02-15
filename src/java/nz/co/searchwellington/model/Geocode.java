package nz.co.searchwellington.model;

public interface Geocode {

    public String getAddress();

    public void setAddress(String address);

    public double getLatitude();

    public void setLatitude(double d);

    public double getLongitude();

    public void setLongitude(double longitude);

    public boolean isValid();

    public boolean isSameLocation(Geocode other);

}