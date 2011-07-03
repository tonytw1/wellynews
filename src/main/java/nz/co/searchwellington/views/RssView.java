package nz.co.searchwellington.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.model.frontend.FrontendResource;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.View;

import com.sun.syndication.feed.synd.SyndEntry;

public class RssView implements View {
	
	private static Logger log = Logger.getLogger(RssView.class);
	
	private RssItemMaker rssItemMaker;
	private RssUrlBuilder rssUrlBuilder;
	
	public RssView(RssItemMaker rssItemMaker, RssUrlBuilder rssUrlBuilder) {
		this.rssItemMaker = rssItemMaker;
		this.rssUrlBuilder = rssUrlBuilder;
	}
	
	@Override
	public String getContentType() {
        return "text/xml";
    }
	
    @SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {        
    	response.setContentType("text/xml;charset=UTF-8");		
    	response.setCharacterEncoding("UTF8");
    	
    	final String rssFeedTitle = rssUrlBuilder.getRssHeadingForGivenHeading((String) model.get("heading"));
    	final List <FrontendResource> content =  (List <FrontendResource>) model.get("main_content");
    	
    	RomeRssFeed outputFeed = new RomeRssFeed(rssFeedTitle, (String) model.get("link"), (String) model.get("description"), makeRssEntiresForContent(content));

    	response.getWriter().print(outputFeed.outputAsXml());
		response.getWriter().flush();
	}
    
	private List<SyndEntry> makeRssEntiresForContent(List<FrontendResource> content) {
		List<SyndEntry> entires = new ArrayList<SyndEntry>();
    	for (FrontendResource item : content) {
			SyndEntry rssItem = rssItemMaker.makeRssItem(item);
			if (rssItem != null) {
				entires.add(rssItem);
			} else {
				log.warn("Could not convert resource to rss item: " + item.getName());
			}
		}
		return entires;
	}
    
}
