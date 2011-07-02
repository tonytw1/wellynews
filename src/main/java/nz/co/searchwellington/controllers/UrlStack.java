package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;

public class UrlStack {
    
    private Logger log = Logger.getLogger(UrlStack.class);
    
    private UrlBuilder urlBuilder;
    
    public UrlStack(UrlBuilder urlBuilder) {
		this.urlBuilder = urlBuilder;
	}
    
	public String getExitUrlFromStack(HttpServletRequest request) {
        String url = urlBuilder.getHomeUrl();
        final String stackUrl = (String) request.getSession().getAttribute("url");        
        if (stackUrl != null) {
            log.debug("Stack url is: " + stackUrl);
            url = urlBuilder.getHomeUrl() + stackUrl;
        }
        log.debug("Exit url from stack is: " + url);
        return url;
    }
    
    public void setUrlStack(HttpServletRequest request) {        
        String url = request.getPathInfo();
        if (request.getQueryString() != null) {
            url = url + "?" + request.getQueryString();
        }
        setUrlStack(request, url);
    }

	public void setUrlStack(HttpServletRequest request, String url) {
		request.getSession().setAttribute("url", url);
        log.debug("Put url onto the stack: " + url);
	}
	
}
