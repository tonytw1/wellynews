package nz.co.searchwellington.geocoding;

import geo.google.GeoAddressStandardizer;
import geo.google.GeoException;
import geo.google.datamodel.GeoAddress;
import nz.co.searchwellington.model.Geocode;

import org.apache.log4j.Logger;

public class GoogleGeoCodeService {

    Logger log = Logger.getLogger(GoogleGeoCodeService.class);

    private String apiKey;
    
    
    public GoogleGeoCodeService() {      
    }

    
    public String getApiKey() {
        return apiKey;
    }


    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }


    // TODO Set scope to NZL / Welly if possible.
    public void resolveAddress(Geocode geocode) {
        if (geocode != null && geocode.getAddress() != null) {
            try {
                GeoAddressStandardizer st = new GeoAddressStandardizer(apiKey);
                GeoAddress geoaddress = st.standardizeToGeoAddress(geocode.getAddress());
                if (geoaddress.getCoordinate() != null) {
                    geocode.setLatitude(geoaddress.getCoordinate().getLatitude());
                    geocode.setLongitude(geoaddress.getCoordinate().getLongitude());
                    log.info("Resolved '" + geocode.getAddress() + "' to " + geocode.getLatitude() + ", " + geocode.getLongitude());
                }
    
            } catch (GeoException e) {
                log.warn(e.getStatus());
                log.warn(e.getMessage());
            }
        } else {
            log.warn("No geocode address to resolve.");
        }

    }

}
