package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;

public class PublisherParmeterFilter implements RequestAttributeFilter {

	private ResourceRepository resourceDAO;
	
	public PublisherParmeterFilter(ResourceRepository resourceDAO) {
		this.resourceDAO = resourceDAO;
	}

	@Override
	public void filter(HttpServletRequest request) {
		if (request.getParameter("publisher") != null && !request.getParameter("publisher").equals("")) {
			String publisherUrlWords = request.getParameter("publisher");
			Website publisher = resourceDAO.getPublisherByUrlWords(publisherUrlWords);
			request.setAttribute("publisher", publisher);
		}
	}
	
}
