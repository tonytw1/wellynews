package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;

public class UrlBuilder {

	private SiteInformation siteInformation;

		
	public UrlBuilder(SiteInformation siteInformation) {		
		this.siteInformation = siteInformation;
	}

	public String getImageUrl(String filename) {
		return siteInformation.getUrl() + "/images/" + filename;
	}

	public String getFeedUrl(Feed feed) {		
		return siteInformation.getUrl() + "/feed/" + feed.getUrlWords();
	}


	public String getTagUrl(Tag tag) {
		return siteInformation.getUrl() + "/" + tag.getName();
	}


	public String getTagCombinerUrl(Tag firstTag, Tag secondTag) {
		return siteInformation.getUrl() + "/" + firstTag.getName() + "+" + secondTag.getName();
	}

	public String getPublisherUrl(Website publisher) {
		return siteInformation.getUrl() + "/" + publisher.getUrlWords();
	}

	public String getPublisherCombinerUrl(Website publisher, Tag tag) {
		return siteInformation.getUrl() + "/" + publisher.getUrlWords() + "+" + tag.getName();
	}

	public String getTagCommentUrl(Tag tag) {
		return siteInformation.getUrl() + "/" + tag.getName() + "/comment";
	}
	
	public String getTagGeocodedUrl(Tag tag) {
		return siteInformation.getUrl() + "/" + tag.getName() + "/geotagged";
	}

	public String getCommentUrl() {
		return siteInformation.getUrl() + "/comment";
	}

	public String getJustinUrl() {
		return siteInformation.getUrl() + "/justin";
	}

	public String getGeotaggedUrl() {
		return siteInformation.getUrl() + "/geotagged";
	}
	
}
