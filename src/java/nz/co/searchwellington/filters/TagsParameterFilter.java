package nz.co.searchwellington.filters;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.TagDAO;

public class TagsParameterFilter implements RequestAttributeFilter {
	
	static Logger log = Logger.getLogger(TagsParameterFilter.class);

	private TagDAO tagDAO;
	
	public TagsParameterFilter(TagDAO tagDAO) {
		this.tagDAO = tagDAO;
	}
	
	public void filter(HttpServletRequest request) {
		if (request.getParameter("tags") != null) {
			String[] tagNames = request.getParameterValues("tags");
			
			List <Tag> tags = new ArrayList <Tag>();
			for (int i = 0; i < tagNames.length; i++) {             
				String tagName = tagNames[i];
				if (tagName != null) {  	// TODO cleaning        
					Tag tag = tagDAO.loadTagByName(tagName);
					if (tag != null) {
						tags.add(tag);
					} else {
						log.warn("Could not find tag with name: " + tagName);
					}
				} 
			}           
			request.setAttribute("tags", tags);
		}
	}

}
