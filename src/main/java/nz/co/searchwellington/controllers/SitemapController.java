package nz.co.searchwellington.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.sitemap.GoogleSitemapService;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

@Controller
public class SitemapController extends MultiActionController {
        
    private SiteInformation siteInformation;
    private GoogleSitemapService sitemapService;
    
    public SitemapController() {
	}
    
    public SitemapController(SiteInformation siteInformation, GoogleSitemapService sitemapService) {
        this.siteInformation = siteInformation;
        this.sitemapService = sitemapService;        
    }
        
    @Transactional(readOnly=true)
    @RequestMapping("/sitemap.xml")
    public ModelAndView sitemap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        
        final String sitemapXml = sitemapService.render(siteInformation.getUrl());        
        mv.addObject("sitemap", sitemapXml);    
        mv.setViewName("sitemap");
        return mv;
    }
    
}
