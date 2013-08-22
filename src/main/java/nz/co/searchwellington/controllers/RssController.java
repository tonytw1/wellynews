package nz.co.searchwellington.controllers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.common.views.rss.RssView;

import com.google.common.collect.Maps;

@Controller	// TODO doesn't need to be a special case?
public class RssController {
	
    private SiteInformation siteInformation;
	private ContentRetrievalService contentRetrievalService;
	private ViewFactory viewFactory;
	
	@Autowired
    public RssController(SiteInformation siteInformation, ContentRetrievalService contentRetrievalService, RssUrlBuilder rssUrlBuilder, ViewFactory viewFactory) {
        this.siteInformation = siteInformation;
        this.contentRetrievalService = contentRetrievalService;
		this.viewFactory = viewFactory;
    }
    
    @RequestMapping("/rss")
    public ModelAndView mainRss(HttpServletRequest request, HttpServletResponse response) throws Exception {    	
		final Map<String, Object> model = Maps.newHashMap();		
		String title = siteInformation.getAreaname() + " Newslog";
		String link = siteInformation.getUrl();
        String description = "Links to " + siteInformation.getAreaname() + " related newsitems.";
        model.put("data", contentRetrievalService.getLatestNewsitems());
        
        final RssView rssView = viewFactory.getRssView(title, link, description);
        return new ModelAndView(rssView, model);        
    }
    
}
