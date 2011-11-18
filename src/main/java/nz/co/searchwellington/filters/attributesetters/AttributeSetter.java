package nz.co.searchwellington.filters.attributesetters;

import javax.servlet.http.HttpServletRequest;

public interface AttributeSetter {
	
	boolean setAttributes(HttpServletRequest request);
	
}
