package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.SiteInformation;

public class UrlBuilder {

	private SiteInformation siteInformation;

		
	public UrlBuilder(SiteInformation siteInformation) {		
		this.siteInformation = siteInformation;
	}


	public String getFeedUrl(Feed feed) {		
		return siteInformation.getUrl() + "/viewfeed?feed=" + feed.getId();
	}

}
