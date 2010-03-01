package nz.co.searchwellington.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;

import org.springframework.web.servlet.View;

import com.sun.syndication.feed.synd.SyndEntry;

public class RssView implements View {

	private SiteInformation siteInformation;
	private RssItemMaker rssItemMaker;	
	
	public RssView(SiteInformation siteInformation, RssItemMaker rssItemMaker) {
		this.siteInformation = siteInformation;
		this.rssItemMaker = rssItemMaker;
	}

	public String getContentType() {
        return "text/xml";
    }

    @SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {        
    	response.setContentType("text/xml;charset=UTF-8");		
    	String rssFeedTitle = (String) model.get("heading") + " - " + siteInformation.getSitename();

    	List <Resource> content =  (List <Resource>) model.get("main_content");
    	List<SyndEntry> entires = new ArrayList<SyndEntry>();
    	for (Resource item : content) {
			entires.add(rssItemMaker.makeRssItem(item));
		}
    			
		RomeRssFeed outputFeed = new RomeRssFeed(rssFeedTitle, (String) model.get("link"), (String) model.get("description"), entires);
		response.getOutputStream().print(outputFeed.outputAsXml());
		response.getOutputStream().flush();
	}
    
}
