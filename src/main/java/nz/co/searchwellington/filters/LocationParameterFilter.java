package nz.co.searchwellington.filters;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.geocoding.osm.CachingNominatimGeocodingService;
import nz.co.searchwellington.geocoding.osm.OsmIdParser;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.Place;

@Component
@Scope(value = "request")
public class LocationParameterFilter implements RequestAttributeFilter {

	private static Logger log = Logger.getLogger(LocationParameterFilter.class);

	public static final String LOCATION = "location";
	public static String RADIUS = "radius";
	
	private static final String LONGITUDE = "longitude";
	private static final String LATITUDE = "latitude";	
	private static final String OSM = "osm";	

	private CachingNominatimGeocodingService geoCodeService;
	private OsmIdParser osmIdParser;
	
	public LocationParameterFilter() {
	}
	
	@Autowired
	public LocationParameterFilter(CachingNominatimGeocodingService geoCodeService, OsmIdParser osmIdParser) {
		this.geoCodeService = geoCodeService;
		this.osmIdParser = osmIdParser;
	}
	
	public void filter(HttpServletRequest request) {		
		final Double radius = processDoubleParameter(request, RADIUS);
		if (radius != null && radius > 0) {
			log.info("Radius attribute set to: " + radius);
			request.setAttribute(RADIUS, radius);
		}
		
		if(request.getParameter(OSM) != null) {
			final String osmIdString = request.getParameter(OSM);
			final OsmId osmId = osmIdParser.parseOsmId(osmIdString);			
			
			final Place resolvedPlace = geoCodeService.resolveOsmId(osmId);			
			log.info("OSM id '" + osmId + "' resolved to: " + resolvedPlace);
			if (resolvedPlace == null) {
				throw new RuntimeException("OSM place could not be resolved");	// TODO 404 in this use case
			}
			
			request.setAttribute(LOCATION, resolvedPlace);
		}
		
		if(request.getParameter(LOCATION) != null) {
			final String location = request.getParameter(LOCATION);
			List<Place> matchingPlaces = geoCodeService.resolveAddress(location);
			if (!matchingPlaces.isEmpty()) {
				final Place firstMatch = matchingPlaces.get(0);
				log.info("User supplied location '" + location + "' resolved to point: " + firstMatch.getLatLong());				
				request.setAttribute(LOCATION, firstMatch);
				
			} else {
				log.info("User supplied location '" + location + "' could not be resolved to a point");
				throw new RuntimeException("Could not resolve place name to lat long");
			}
		}
		
		final Double latitude = processDoubleParameter(request, LATITUDE);
		final Double longitude = processDoubleParameter(request, LONGITUDE);
		if (latitude != null && longitude != null) {
			// TODO Should try todo a reverse lookup to name this location.
			LatLong latLong = new LatLong(latitude, longitude);
			request.setAttribute(LOCATION, new Place(latLong.toString(), latLong, null));
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
