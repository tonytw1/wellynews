package nz.co.searchwellington.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nz.co.searchwellington.geocoding.GeoCodeService;
import nz.co.searchwellington.model.Geocode;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

public class LocationParameterFilterTest {
	
	private static final String VALID_LOCATION = "Petone Station";
	private static final String INVALID_LOCATION = "Twickenham Station, Wellington";
	
	@Mock GeoCodeService geocodeService;
	MockHttpServletRequest request;
	private LocationParameterFilter filter;
	private Geocode petoneStation;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		filter = new LocationParameterFilter(geocodeService);
		petoneStation = new Geocode(VALID_LOCATION, 1, 2);
	}

	@Test
	public void canResolveNamedPlaceAsLocation() throws Exception {
		request.setParameter("location", VALID_LOCATION);
		Mockito.when(geocodeService.resolveAddress(VALID_LOCATION)).thenReturn(petoneStation);
		
		filter.filter(request);
		
		Geocode locationAttribute = (Geocode) request.getAttribute(LocationParameterFilter.LOCATION);	
		assertTrue(locationAttribute.isValid());
		assertEquals(VALID_LOCATION, locationAttribute.getAddress());
	}
	
	@Test
	public void canResolveAbsoluteLatLongPointAsALocation() throws Exception {
		request.setParameter("latitude", "51.2");
		request.setParameter("longitude", "-0.1");
		
		filter.filter(request);
		
		Geocode locationAttribute = (Geocode) request.getAttribute(LocationParameterFilter.LOCATION);	
		assertTrue(locationAttribute.isValid());
		assertEquals(51.2, locationAttribute.getLatitude(), 0);
		assertEquals(-0.1, locationAttribute.getLongitude(), 0);
	}
	
	@Test
	public void locationAttributeShouldBeMarkedAsInvalidIfLocationCouldNotBeResolved() throws Exception {
		request.setParameter("location", INVALID_LOCATION);
		
		filter.filter(request);

		Geocode locationAttribute = (Geocode) request.getAttribute(LocationParameterFilter.LOCATION);	
		assertFalse(locationAttribute.isValid());
		assertEquals(INVALID_LOCATION, locationAttribute.getAddress());
	}
	
}
