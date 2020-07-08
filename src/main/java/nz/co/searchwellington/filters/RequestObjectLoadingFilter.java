package nz.co.searchwellington.filters;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component("requestObjectLoadingFilter")
public class RequestObjectLoadingFilter implements Filter {

	private final static Logger log = Logger.getLogger(RequestObjectLoadingFilter.class);

	private RequestFilter requestFilter;
	private LoggedInUserFilter loggedInUserFilter;
	
	public RequestObjectLoadingFilter() {
	}
	
	@Autowired
	public RequestObjectLoadingFilter(RequestFilter requestFilter, LoggedInUserFilter loggedInUserFilter) {	
		this.requestFilter = requestFilter;
		this.loggedInUserFilter = loggedInUserFilter;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		log.info("Filtering");
		requestFilter.loadAttributesOntoRequest((HttpServletRequest) request);
		loggedInUserFilter.loadLoggedInUser((HttpServletRequest) request);
		chain.doFilter (request, response);
	}
	
	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {		
	}
	
}
