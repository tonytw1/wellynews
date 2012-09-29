package nz.co.searchwellington.views;

import nz.co.searchwellington.controllers.RssUrlBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

@Component
public class RssViewFactory {
	
	private RssItemMaker rssItemMaker;
	private RssUrlBuilder rssUrlBuilder;

	@Autowired
	public RssViewFactory(RssItemMaker rssItemMaker, RssUrlBuilder rssUrlBuilder) {
		this.rssItemMaker = rssItemMaker;
		this.rssUrlBuilder = rssUrlBuilder;
	}

	public View makeView() {
		return new RssView(rssItemMaker, rssUrlBuilder);
	}
	
}
