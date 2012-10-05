package nz.co.searchwellington.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.geocoding.CachingServiceWrapper;
import nz.co.searchwellington.geocoding.osm.CachingNominatimGeocodingService;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.OsmId;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

public class LocationParameterFilterTest {
	
	private static final String VALID_LOCATION = "Petone Station";
	private static final String INVALID_LOCATION = "Twickenham Station, Wellington";
	
	@Mock private CachingNominatimGeocodingService geocodeService;
	@Mock private CachingServiceWrapper<OsmId, Geocode> osmGeocodeService;
	
	private MockHttpServletRequest request;
	private Geocode petoneStation;
	
	private LocationParameterFilter filter;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		filter = new LocationParameterFilter(geocodeService, osmGeocodeService);
		petoneStation = new Geocode(VALID_LOCATION, 1.1, 2.2);
	}

	@Test
	public void canResolveNamedPlaceAsLocation() throws Exception {
		request.setParameter("location", VALID_LOCATION);
		List<Geocode> results = new ArrayList<Geocode>();
		results.add(petoneStation);
		Mockito.when(geocodeService.resolveAddress(VALID_LOCATION)).thenReturn(results);
		
		filter.filter(request);
		
		Geocode locationAttribute = (Geocode) request.getAttribute(LocationParameterFilter.LOCATION);	
		assertTrue(locationAttribute.isValid());
		assertEquals(VALID_LOCATION, locationAttribute.getAddress());
	}
	
	@Test
	public void canResolveLocationSearchRadius() throws Exception {
		request.setParameter("radius", "3");
		filter.filter(request);		
		assertEquals(3.0, request.getAttribute(LocationParameterFilter.RADIUS));
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
