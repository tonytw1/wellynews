package nz.co.searchwellington.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.urls.UrlBuilder;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public class ClickThroughController implements Controller {
    
    private static Logger log = Logger.getLogger(ClickThroughController.class);
    
    private UrlBuilder urlBuilder;
    
    public ClickThroughController(UrlBuilder urlBuilder) {
		this.urlBuilder = urlBuilder;
	}
    
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();        
        String redirectUrl = null;
        Resource resource = (Resource) request.getAttribute("resource");
        if (resource != null) {
            log.info("Found resource on request; redirecting to resource url: " + resource.getUrl());
            redirectUrl = resource.getUrl();
        } else {
            log.info("Could not find a resource on the request; redirecting to front.");
            redirectUrl = urlBuilder.getHomeUrl();           
        }
        
        View redirectView = new RedirectView(redirectUrl);       
        mv.setView(redirectView);
        return mv;
    }

    
    
}
