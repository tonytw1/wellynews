package nz.co.searchwellington.filters;

import com.google.common.base.Strings;
import nz.co.searchwellington.exceptions.UnresolvableLocationException;
import nz.co.searchwellington.geocoding.osm.GeoCodeService;
import nz.co.searchwellington.geocoding.osm.OsmIdParser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.Place;

import javax.servlet.http.HttpServletRequest;

@Component
@Scope(value = "request")
public class LocationParameterFilter implements RequestAttributeFilter {

	private static Logger log = Logger.getLogger(LocationParameterFilter.class);

	public static final String LOCATION = "location";
	public static String RADIUS = "radius";
	
	private static final String LATITUDE = "latitude";	
	private static final String LONGITUDE = "longitude";
	private static final String OSM = "osm";	

	private GeoCodeService geoCodeService;
	private OsmIdParser osmIdParser;
	
	public LocationParameterFilter() {
	}
	
	@Autowired
	public LocationParameterFilter(GeoCodeService geoCodeService, OsmIdParser osmIdParser) {
		this.geoCodeService = geoCodeService;
		this.osmIdParser = osmIdParser;
	}
	
	public void filter(HttpServletRequest request) {		
		final Double radius = processDoubleParameter(request, RADIUS);
		if (radius != null && radius > 0) {
			log.debug("Radius attribute set to: " + radius);
			request.setAttribute(RADIUS, radius);
		}

		if(!Strings.isNullOrEmpty(request.getParameter(OSM))) {
			final String osmIdString = request.getParameter(OSM);
			final OsmId osmId = osmIdParser.parseOsmId(osmIdString);
			final Place resolvedPlace = geoCodeService.resolveOsmId(osmId);
			log.debug("OSM id '" + osmId + "' resolved to: " + resolvedPlace);
			if (resolvedPlace == null) {
				throw new UnresolvableLocationException("OSM place could not be resolved");
			}
			request.setAttribute(LOCATION, resolvedPlace);
			return;
		}
				
		if (!Strings.isNullOrEmpty(request.getParameter("latitude")) && !Strings.isNullOrEmpty(request.getParameter("longitude"))) {
			final LatLong latLong = new LatLong(Double.parseDouble((String) request.getParameter(LATITUDE)), 
					Double.parseDouble((String) request.getParameter(LONGITUDE)));
			final String latLongLabel = latLong.getLatitude() + ", " + latLong.getLongitude(); 
			// TODO - is you wanted to, you could resolve for a name, but don't alter the user supplied lat/long values.
			// TODO lat, long isn't really an address - this should be something like a display method on latLong or the view which gives a sensible output when address is null.
			request.setAttribute(LOCATION, new Place(latLongLabel, latLong, null));
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
