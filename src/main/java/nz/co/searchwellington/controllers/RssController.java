package nz.co.searchwellington.controllers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.repositories.ContentRetrievalService;
import nz.co.searchwellington.views.RssItemMaker;
import nz.co.searchwellington.views.RssView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Maps;

@Controller
public class RssController {
	    
    private static final int MAX_RSS_ITEMS = 30;	// TODO move this knowledge towards the CRS
    
    private SiteInformation siteInformation;
	private ContentRetrievalService contentRetrievalService;
	private RssItemMaker rssItemMaker;
	private RssUrlBuilder rssUrlBuilder;
	
	@Autowired
    public RssController(SiteInformation siteInformation, ContentRetrievalService contentRetrievalService, RssItemMaker rssItemMaker, RssUrlBuilder rssUrlBuilder) {
        this.siteInformation = siteInformation;
        this.contentRetrievalService = contentRetrievalService;
        this.rssItemMaker = rssItemMaker;
        this.rssUrlBuilder = rssUrlBuilder;
    }
    
    @RequestMapping("/rss")
    public ModelAndView mainRss(HttpServletRequest request, HttpServletResponse response) throws Exception {    	
		final Map<String, Object> model = Maps.newHashMap();
		model.put("heading", siteInformation.getAreaname() + " Newslog");
		model.put("link", siteInformation.getUrl());
        model.put("description", "Links to " + siteInformation.getAreaname() + " related newsitems.");
        model.put("main_content", contentRetrievalService.getLatestNewsitems(MAX_RSS_ITEMS));
        
        final RssView rssView = new RssView(rssItemMaker, rssUrlBuilder);	// TODO use viewfactory
        return new ModelAndView(rssView, model);        
    }
    
}
