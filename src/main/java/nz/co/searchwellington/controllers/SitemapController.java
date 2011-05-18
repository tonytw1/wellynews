package nz.co.searchwellington.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.sitemap.GoogleSitemapService;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;


public class SitemapController extends MultiActionController {
    
    Logger log = Logger.getLogger(SitemapController.class);
    
    private SiteInformation siteInformation;
    private GoogleSitemapService sitemapService;
     
    public SitemapController(SiteInformation siteInformation, GoogleSitemapService sitemapService) {
        this.siteInformation = siteInformation;
        this.sitemapService = sitemapService;        
    }
        
    @Transactional	// TODO read only
    public ModelAndView sitemap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        
        final String sitemapXml = sitemapService.render(siteInformation.getUrl());        
        mv.addObject("sitemap", sitemapXml);    
        mv.setViewName("sitemap");
        return mv;
    }
    
}
