package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class PageParameterFilter implements RequestAttributeFilter {

	public final static String PAGE_ATTRIBUTE = "page";

	public void filter(HttpServletRequest request) {
		if (request.getParameter(PAGE_ATTRIBUTE) != null) {
			String pageString = request.getParameter(PAGE_ATTRIBUTE);
			try {
				Integer page = Integer.parseInt(pageString);
				request.setAttribute(PAGE_ATTRIBUTE, page);
			} catch (NumberFormatException e) {
			}
		}
	}

}
