package nz.co.searchwellington.geocoding;

import org.junit.Test;

public class GoogleGeoCodeServiceFunctionalTest {

	@Test
	public void canResolveFromLiveService() throws Exception {
		GoogleGeoCodeService service = new GoogleGeoCodeService();
		service.resolveAddress("49 Southgate Road, Island bay");
		service.resolveAddress("Island bay");
		service.resolveAddress("Wellington Zoo");
		service.resolveAddress("Basin reserve, Wellington");
		service.resolveAddress("Wakefield Park");
		service.resolveAddress("Petone Station");
		service.resolveAddress("Petone");

	}
}
