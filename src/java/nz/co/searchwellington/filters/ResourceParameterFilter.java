package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.ResourceRepository;

public class ResourceParameterFilter implements RequestAttributeFilter {
	
	static Logger log = Logger.getLogger(ResourceParameterFilter.class);
	
	private ResourceRepository resourceDAO;
	
	public ResourceParameterFilter(ResourceRepository resourceDAO) {
		this.resourceDAO = resourceDAO;
	}
	
	public void filter(HttpServletRequest request) {
		if (request.getParameter("resource") != null) {
			String resourceParametere = request.getParameter("resource");			
			try {
				final int resourceId = Integer.parseInt(resourceParametere);
				if (resourceId > 0) {
					Resource resource = resourceDAO.loadResourceById(resourceId);
					if (resource != null) {
						log.info("Found resource: " + resource.getName());
						request.setAttribute("resource", resource);
						return;
					}
				}
			} catch (NumberFormatException e) {
				log.warn("Invalid resource id given: " + resourceParametere);
			}
		}
		
	}

}
