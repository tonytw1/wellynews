package nz.co.searchwellington.filters;

import nz.co.searchwellington.controllers.LoggedInUserFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("requestObjectLoadingFilter")
public class RequestObjectLoadingFilter implements Filter {

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
        requestFilter.loadAttributesOntoRequest((HttpServletRequest) request);
        loggedInUserFilter.loadLoggedInUser((HttpServletRequest) request);

        // TODO move to seperate filter
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

}
