package nz.co.searchwellington.controllers.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.geocoding.GeoCodeService;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.views.JsonViewFactory;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class GeocodeAjaxController implements Controller {
	
	private GeoCodeService geoCodeService;
	private JsonViewFactory jsonViewFactory;
	
	public GeocodeAjaxController(GeoCodeService geoCodeService, JsonViewFactory jsonViewFactory) {		
		this.geoCodeService = geoCodeService;
		this.jsonViewFactory = jsonViewFactory;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final ModelAndView mv = new ModelAndView(jsonViewFactory.makeView());
        List<Geocode> addresses = new ArrayList<Geocode>();
        if (request.getParameter("q") != null) {
        	addresses = geoCodeService.resolveAddress(request.getParameter("q"));
        }
        mv.addObject(addresses);
        return mv;
    }
    
}
