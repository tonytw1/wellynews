package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class LocationParameterFilter implements RequestAttributeFilter {

	static Logger log = Logger.getLogger(LocationParameterFilter.class);

	public static final String LONGITUDE = "longitude";
	public static final String LATITUDE = "latitude";
	
	public void filter(HttpServletRequest request) {		
		processDoubleParameter(request, LATITUDE);
		processDoubleParameter(request, LONGITUDE);		
	}

	private void processDoubleParameter(HttpServletRequest request, String parameterName) {
		if (request.getParameter(parameterName) != null) {
			try {
				request.setAttribute(parameterName, Double.parseDouble(request.getParameter(parameterName)));
			} catch (NumberFormatException e) {
				log.warn("User supplied invalid " + parameterName + " value: " + request.getParameter(parameterName));
			}
		}
	}

}
