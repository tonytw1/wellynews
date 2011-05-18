package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

public interface RequestAttributeFilter {

	public void filter(HttpServletRequest request);

}