package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.TagDAO;

// TODO depricate be using a url tagname instead of a form parameter - move to adminFilter?
// Used by the rssfeeds index page?
public class TagParameterFilter implements RequestAttributeFilter {

	private TagDAO tagDAO;
	
	public TagParameterFilter(TagDAO tagDAO) {
		super();
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
