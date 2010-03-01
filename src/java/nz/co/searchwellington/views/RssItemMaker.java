package nz.co.searchwellington.views;

import nz.co.searchwellington.model.Resource;

import com.sun.syndication.feed.synd.SyndEntry;

public class RssItemMaker {
	
	public SyndEntry makeRssItem(Resource content) {
		return content.getRssItem();
	}

}
