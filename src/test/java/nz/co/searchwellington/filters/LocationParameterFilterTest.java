package nz.co.searchwellington.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nz.co.searchwellington.geocoding.osm.CachingNominatimGeocodingService;
import nz.co.searchwellington.model.Geocode;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.Place;

import com.google.common.collect.Lists;

public class LocationParameterFilterTest {
	
	private static final String VALID_LOCATION = "Petone Station";
	private static final String INVALID_LOCATION = "Twickenham Station, Wellington";
	
	@Mock private CachingNominatimGeocodingService geocodeService;
	
	private MockHttpServletRequest request;
	private Place petoneStation;
	
	private LocationParameterFilter filter;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		request = new MockHttpServletRequest();
		filter = new LocationParameterFilter(geocodeService);
		petoneStation = new Place(VALID_LOCATION, new LatLong(1.1, 2.2), null);
	}

	@Test
	public void canResolveNamedPlaceAsLocation() throws Exception {
		request.setParameter("location", VALID_LOCATION);
		List<Place> results = Lists.newArrayList();
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
