package nz.co.searchwellington.filters;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.geocoding.CachingGeocodeService;
import nz.co.searchwellington.model.Geocode;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request")
public class LocationParameterFilter implements RequestAttributeFilter {

	private static Logger log = Logger.getLogger(LocationParameterFilter.class);

	public static final String LOCATION = "location";
	public static String RADIUS = "radius";
	
	private static final String LONGITUDE = "longitude";
	private static final String LATITUDE = "latitude";	
	private static final String OSM = "osm";	

	private CachingGeocodeService geoCodeService;
	
	@Autowired
	public LocationParameterFilter(CachingGeocodeService geoCodeService) {
		this.geoCodeService = geoCodeService;
	}
	
	public void filter(HttpServletRequest request) {		
		Double radius = processDoubleParameter(request, RADIUS);
		if (radius != null && radius > 0) {
			log.info("Radius attribute set to: " + radius);
			request.setAttribute(RADIUS, radius);
		}
		
		if(request.getParameter(OSM) != null) {
			final String osm = request.getParameter(OSM);
			final Geocode resolvedOsmPlace = geoCodeService.resolveAddress(osm.split("/")[1], Long.parseLong(osm.split("/")[0]));
			log.info("OSM place '" + osm + "' resolved to: " + resolvedOsmPlace);
			request.setAttribute(LOCATION, resolvedOsmPlace);
		}
		
		if(request.getParameter(LOCATION) != null) {
			final String location = request.getParameter(LOCATION);
			List<Geocode> resolvedGeocode = geoCodeService.resolveAddress(location);
			if (resolvedGeocode != null && !resolvedGeocode.isEmpty() && resolvedGeocode.get(0).isValid()) {
				final Geocode firstMatch = resolvedGeocode.get(0);
				log.info("User supplied location '" + location + "' resolved to point: " + firstMatch.getLatitude() + ", " + firstMatch.getLongitude());				
				request.setAttribute(LOCATION, firstMatch);
				
			} else {
				log.info("User supplied location '" + location + "' could not be resolved to a point; marking as invalid");
				request.setAttribute(LOCATION, new Geocode(location, null, null));
			}
			return;
		}
		
		final Double latitude = processDoubleParameter(request, LATITUDE);
		final Double longitude = processDoubleParameter(request, LONGITUDE);
		if (latitude != null && longitude != null) {
			// TODO Should try todo a reverse lookup to name this location.
			final Geocode specificPointGeocode = new Geocode(latitude, longitude);
			request.setAttribute(LOCATION, specificPointGeocode);
		}
		
	}
	
	private Double processDoubleParameter(HttpServletRequest request, String parameterName) {
		if (request.getParameter(parameterName) != null) {
			try {
				return Double.parseDouble(request.getParameter(parameterName));
			} catch (NumberFormatException e) {
				log.warn("User supplied invalid double " + parameterName + " value: " + request.getParameter(parameterName));
			}
		}
		return null;
	}

}
