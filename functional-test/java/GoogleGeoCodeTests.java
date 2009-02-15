import junit.framework.TestCase;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.GeocodeImpl;
import nz.co.searchwellington.repositories.GoogleGeoCodeService;


public class GoogleGeoCodeTests extends TestCase {

    
    
    public void testCanResolveNewtown() throws Exception {                
        final String apiKey = "ABQIAAAAsRzTufcmVSow_p4e33JuaBT-4N31URYZoEh7JlL-HblmMY0A2RSWxJuZronoLLlbhrUUFinnJG6jlA";        
        GoogleGeoCodeService geoCodeService = new GoogleGeoCodeService(apiKey);
        
        Geocode southgateRoadGeocode = new GeocodeImpl("49 southgate Road, Wellington, New Zealand");
        geoCodeService.resolveAddress(southgateRoadGeocode);                
        
        assertEquals(-41.335583, southgateRoadGeocode.getLatitude());
        assertEquals(174.782562, southgateRoadGeocode.getLongitude());
    }
   
}
