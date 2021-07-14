package nz.co.searchwellington.filters;

import static org.junit.Assert.assertEquals;

import java.util.List;

import nz.co.searchwellington.geocoding.osm.CachingNominatimGeocodingService;
import nz.co.searchwellington.geocoding.osm.OsmIdParser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.OsmType;
import uk.co.eelpieconsulting.common.geo.model.Place;

import com.google.common.collect.Lists;

public class LocationParameterFilterTest {
	
	private static final String VALID_LOCATION = "Petone Station";

	private CachingNominatimGeocodingService geocodeService = Mockito.mock(CachingNominatimGeocodingService.class);

	private MockHttpServletRequest request;
	private Place petoneStation = new Place(VALID_LOCATION, new LatLong(1.1, 2.2), new OsmId(123, OsmType.NODE));

	private LocationParameterFilter filter = new LocationParameterFilter(geocodeService, new OsmIdParser());

	@Test
	public void canResolveOsmIdAsLocation() throws Exception {
		request.setParameter("osm", "123/NODE");
		System.out.println(petoneStation.getOsmId());
		Mockito.when(geocodeService.resolveOsmId(petoneStation.getOsmId())).thenReturn(petoneStation);

		filter.filter(request);

		final Place locationAttribute = (Place) request.getAttribute(LocationParameterFilter.LOCATION);
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
		
		final Place locationAttribute = (Place) request.getAttribute(LocationParameterFilter.LOCATION);	
		assertEquals(51.2, locationAttribute.getLatLong().getLatitude(), 0);
		assertEquals(-0.1, locationAttribute.getLatLong().getLongitude(), 0);
	}
	
}
