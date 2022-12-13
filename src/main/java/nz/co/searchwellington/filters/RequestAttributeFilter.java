package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

public interface RequestAttributeFilter {	// TODO this probably the same as Attribute setter

	public void filter(HttpServletRequest request);

}