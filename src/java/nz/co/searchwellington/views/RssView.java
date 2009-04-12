package nz.co.searchwellington.views;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.RomeRssFeed;
import nz.co.searchwellington.model.RssFeedable;

import org.springframework.web.servlet.View;


public class RssView implements View {

    private String clickThroughUrl;

    // TODO really - should implement this?
	public String getContentType() {
        return null;
    }

    public void render(Map model, HttpServletRequest req, HttpServletResponse res) throws Exception {
        
        List <RssFeedable> mainContent =  (List <RssFeedable>) model.get("main_content");        
		res.setContentType("text/xml");
        res.setCharacterEncoding("UTF-8");
        
        RomeRssFeed outputFeed = new RomeRssFeed(	(String) model.get("heading"), 
        		(String) model.get("link"), 
        		(String) model.get("description"), 
        		mainContent, clickThroughUrl);
        res.getOutputStream().print(outputFeed.outputAsXml());        
		res.getOutputStream().flush();
	}

 
    public void setClickThroughUrl(String clickThroughUrl) {
        this.clickThroughUrl = clickThroughUrl;
    }

}
