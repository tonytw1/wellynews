package nz.co.searchwellington.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import nz.co.searchwellington.model.ArchiveLink;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;

public class UrlBuilder {

	private SiteInformation siteInformation;

		
	public UrlBuilder(SiteInformation siteInformation) {		
		this.siteInformation = siteInformation;
	}

	public String getImageUrl(String filename) {
		return siteInformation.getImageRoot() + filename;
	}
	
	public String getStaticUrl(String filename) {
		return siteInformation.getStaticRoot() + filename;
	}
	
	public String getPublishersAutoCompleteUrl() {
		return siteInformation.getUrl() + "/ajax/publishers";
	}
	
	
	public String getFeedUrl(Feed feed) {		
		return siteInformation.getUrl() + "/feed/" + feed.getUrlWords();
	}
	
	public String getFeedsInboxUrl() {
		return siteInformation.getUrl() + "/feeds/inbox";
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
	
	public String getArchiveLinkUrl(ArchiveLink archiveLink) {
		// TODO if this dateformatter thread safe? Replace with DateFormatter
		Date month = archiveLink.getMonth();
		SimpleDateFormat df = new SimpleDateFormat();
	    df.applyPattern("yyyy");
	    String yearString = df.format(month.getTime());
	    df.applyPattern("MMM");
	    String monthString = df.format(month.getTime());
	    return siteInformation.getUrl() + "/archive/" + yearString + "/" + monthString.toLowerCase();		
	}

	public String getOpenIDCallbackUrl() {
		return siteInformation.getUrl() + "/openid/callback";
	}
	
	
	public String getProfileUrl() {
		return siteInformation.getUrl() + "/profile";
	}
	
	public String getProfileUrl(User user) {
		return siteInformation.getUrl() + "/profile/" + user.getProfilename();
	}
	
}
