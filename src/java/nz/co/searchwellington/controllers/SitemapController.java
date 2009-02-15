package nz.co.searchwellington.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.sitemap.GoogleSitemapService;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;


public class SitemapController extends BaseMultiActionController {
    
    Logger log = Logger.getLogger(SitemapController.class);
    
    private SiteInformation siteInformation;
    private GoogleSitemapService sitemapService;
     
    public SitemapController(ResourceRepository resourceDAO, SiteInformation siteInformation, GoogleSitemapService sitemapService) {       
        this.resourceDAO = resourceDAO;
        this.siteInformation = siteInformation;
        this.sitemapService = sitemapService;
    }
    
    
    @Transactional
    public ModelAndView sitemap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        
        List<Tag> tags = resourceDAO.getAllTags();
        final String sitemapXml = sitemapService.render(tags, siteInformation.getUrl());
        
        mv.addObject("sitemap", sitemapXml);    
        mv.setViewName("sitemap");
        return mv;
    }
    
}
    