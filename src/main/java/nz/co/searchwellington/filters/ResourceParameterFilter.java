package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
@Scope("request")
public class ResourceParameterFilter implements RequestAttributeFilter {
	
	private static Logger log = Logger.getLogger(ResourceParameterFilter.class);
	
	private HibernateResourceDAO resourceDAO;
	
	@Autowired
	public ResourceParameterFilter(HibernateResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}
	
	public void filter(HttpServletRequest request) {
		final String resourceParameter = request.getParameter("resource");
		if (Strings.isNullOrEmpty(resourceParameter)) {
			return;
		}
		
		if (resourceParameter.matches("\\d+")) {
			processResourceId(request, resourceParameter);		
		}
		
		processResourceUrlWords(request, resourceParameter);
	}
	
	private void processResourceId(HttpServletRequest request, String resourceParameter) {
		try {
			final int resourceId = Integer.parseInt(resourceParameter);
			if (resourceId > 0) {
				Resource resource = resourceDAO.loadResourceById(resourceId);
				if (resource != null) {
					log.info("Found resource: " + resource.getName());
					request.setAttribute("resource", resource);
					return;
				}
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid resource id given: " + resourceParameter);
		}
	}
	
	private void processResourceUrlWords(HttpServletRequest request, String resourceParameter) {
		final Resource resource = resourceDAO.loadByUrlWords(resourceParameter);
		if (resource != null) {
			log.info("Found resourc by urlWords: " + resource.getName());
			request.setAttribute("resource", resource);
			return;
		}
	}
	
}
