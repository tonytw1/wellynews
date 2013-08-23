package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.TagDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

// TODO depricate be using a url tagname instead of a form parameter - move to adminFilter?
// Used by the rssfeeds index page?
@Component
@Scope("request")
public class TagParameterFilter implements RequestAttributeFilter {

	private TagDAO tagDAO;
	
	@Autowired
	public TagParameterFilter(TagDAO tagDAO) {
		this.tagDAO = tagDAO;
	}

	@Override
	public void filter(HttpServletRequest request) {
		if (request.getParameter("tag") != null) {
			String tagName = request.getParameter("tag");
			Tag tag = tagDAO.loadTagByName(tagName);
			request.setAttribute("tag", tag);
		}
	}
	
}
