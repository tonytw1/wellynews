package nz.co.searchwellington.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class TestFilter implements Filter{

	private RequestFilter requestFilter;
	
	
	public TestFilter(RequestFilter requestFilter) {	
		this.requestFilter = requestFilter;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		requestFilter.loadAttributesOntoRequest((HttpServletRequest) request);
		chain.doFilter (request, response);
	}
	
	
	public void destroy() {
		// TODO Auto-generated method stub		
	}


	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	
	 
	 
}
