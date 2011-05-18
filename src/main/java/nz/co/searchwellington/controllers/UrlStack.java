package nz.co.searchwellington.controllers;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import nz.co.searchwellington.model.SiteInformation;

public class UrlStack {
    
    Logger log = Logger.getLogger(UrlStack.class);
    
    
    private SiteInformation siteInformation;
    
   
    public UrlStack(SiteInformation siteInformation) {
        this.siteInformation = siteInformation;
    }


    public String getExitUrlFromStack(HttpServletRequest request) {
        String url = siteInformation.getUrl() + "/index";
        final String stackUrl = (String) request.getSession().getAttribute("url");        
        if (stackUrl != null) {
            log.debug("Stack url is: " + stackUrl);
            url = (String) siteInformation.getUrl() + stackUrl;
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
