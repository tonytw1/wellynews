package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.geocoding.GoogleGeoCodeService;
import nz.co.searchwellington.model.Geocode;

import org.apache.log4j.Logger;

public class LocationParameterFilter implements RequestAttributeFilter {

	static Logger log = Logger.getLogger(LocationParameterFilter.class);
	
	public static final String LONGITUDE = "longitude";
	public static final String LATITUDE = "latitude";
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
				request.setAttribute(LOCATION, location);
				request.setAttribute(LATITUDE, resolvedGeocode.getLatitude());
				request.setAttribute(LONGITUDE, resolvedGeocode.getLongitude());
				
			} else {
				log.info("User supplied location ' + " + location + "' could not be resolved to a point; ignoring.");
			}
			return;
		}
		
		processDoubleParameter(request, LATITUDE);
		processDoubleParameter(request, LONGITUDE);		
	}
	
	private void processDoubleParameter(HttpServletRequest request, String parameterName) {
		if (request.getParameter(parameterName) != null) {
			try {
				request.setAttribute(parameterName, Double.parseDouble(request.getParameter(parameterName)));
				log.info("Set location attribute '" + parameterName + "' to: " + request.getAttribute(parameterName));
			} catch (NumberFormatException e) {
				log.warn("User supplied invalid " + parameterName + " value: " + request.getParameter(parameterName));
			}
		}
	}

}
