package nz.co.searchwellington.views;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.RssFeedable;
import nz.co.searchwellington.model.SiteInformation;

import org.springframework.web.servlet.View;

public class RssView implements View {

	private SiteInformation siteInformation;
		
	public RssView(SiteInformation siteInformation) {		
		this.siteInformation = siteInformation;
	}

	public String getContentType() {
        return "text/xml";
    }

    @SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest req, HttpServletResponse res) throws Exception {        
        List <RssFeedable> mainContent =  (List <RssFeedable>) model.get("main_content");
		res.setContentType("text/xml;charset=UTF-8");		
		
        String rssFeedTitle = (String) model.get("heading") + " - " + siteInformation.getSitename();
		RomeRssFeed outputFeed = new RomeRssFeed(	rssFeedTitle, 
        		(String) model.get("link"), 
        		(String) model.get("description"), 
        		mainContent);
        res.getOutputStream().print(outputFeed.outputAsXml());        
		res.getOutputStream().flush();		
	}
    
}
