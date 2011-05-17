package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.geocoding.GoogleGeoCodeService;
import nz.co.searchwellington.model.Geocode;

import org.apache.log4j.Logger;

public class LocationParameterFilter implements RequestAttributeFilter {

	private static Logger log = Logger.getLogger(LocationParameterFilter.class);
		
	private static final String LONGITUDE = "longitude";
	private static final String LATITUDE = "latitude";
	
	public static final String LOCATION = "location";

	private GoogleGeoCodeService geoCodeService;
	
	public LocationParameterFilter(GoogleGeoCodeService geoCodeService) {
		this.geoCodeService = geoCodeService;
	}

	public void filter(HttpServletRequest request) {		
		if(request.getParameter(LOCATION) != null) {
			final String location = request.getParameter(LOCATION);
			Geocode resolvedGeocode = new Geocode(location);
			geoCodeService.resolveAddress(resolvedGeocode);
			if (resolvedGeocode.isValid()) {
				log.info("User supplied location '" + location + "' resolved to point: " + resolvedGeocode.getLatitude() + ", " + resolvedGeocode.getLongitude());				
				request.setAttribute(LOCATION, resolvedGeocode);			
				
			} else {
				log.info("User supplied location ' + " + location + "' could not be resolved to a point; ignoring.");
			}
			return;
		}
		
		Double latitude = processDoubleParameter(request, LATITUDE);
		Double longitude = processDoubleParameter(request, LONGITUDE);
		if (latitude != null && longitude != null) {
			// TODO Should try todo a reverse lookup to name this location.
			Geocode specificPointGeocode = new Geocode(latitude, longitude);
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
