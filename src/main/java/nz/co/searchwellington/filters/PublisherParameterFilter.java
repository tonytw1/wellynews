package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.HibernateResourceDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Deprecated
@Component
@Scope("request")
public class PublisherParameterFilter implements RequestAttributeFilter {

	private HibernateResourceDAO resourceDAO;
	
	@Autowired
	public PublisherParameterFilter(HibernateResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}
	
	@Override
	public void filter(HttpServletRequest request) {
		if (!Strings.isNullOrEmpty(request.getParameter("publisher"))) {
			String publisherUrlWords = request.getParameter("publisher");
			Website publisher = resourceDAO.getPublisherByUrlWords(publisherUrlWords);
			request.setAttribute("publisher", publisher);
		}
	}
	
}
