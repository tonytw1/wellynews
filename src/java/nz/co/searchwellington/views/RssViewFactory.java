package nz.co.searchwellington.views;

import org.springframework.web.servlet.View;

import nz.co.searchwellington.model.SiteInformation;

public class RssViewFactory {
	
	private SiteInformation siteInformation;
	private RssItemMaker rssItemMaker;

	public RssViewFactory(SiteInformation siteInformation, RssItemMaker rssItemMaker) {
		this.siteInformation = siteInformation;
		this.rssItemMaker = rssItemMaker;
	}

	public View makeView() {
		return new RssView(siteInformation, rssItemMaker);
	}
	
}
