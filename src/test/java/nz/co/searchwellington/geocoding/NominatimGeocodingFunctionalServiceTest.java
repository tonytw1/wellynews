package nz.co.searchwellington.geocoding;

import static org.junit.Assert.assertEquals;
import nz.co.searchwellington.model.Geocode;

import org.junit.Test;

public class NominatimGeocodingFunctionalServiceTest {

	@Test
	public void canResolveBuildingNameToLocation() {
		NominatimGeocodingService service = new NominatimGeocodingService();		
		Geocode result = service.resolveAddress("St James Presbyterian Church, Newtown, Wellington");
		assertEquals("St James' Presbyterian Church, Adelaide Road, Newtown, Wellington, 6021, Aotearoa New Zealand", result.getAddress());
	}

}
