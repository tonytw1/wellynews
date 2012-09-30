package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class PageParameterFilter implements RequestAttributeFilter {

	public void filter(HttpServletRequest request) {
		if (request.getParameter("page") != null) {
			String pageString = request.getParameter("page");
			try {
				Integer page = Integer.parseInt(pageString);
				request.setAttribute("page", page);
			} catch (NumberFormatException e) {
			}
		}
	}

}
