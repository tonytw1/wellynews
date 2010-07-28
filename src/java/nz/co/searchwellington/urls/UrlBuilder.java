package nz.co.searchwellington.urls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.UrlWordsGenerator;
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
	
	public String getTagsAutoCompleteUrl() {
		return siteInformation.getUrl() + "/ajax/tags";
	}
		
	public String getFeedUrl(Feed feed) {		
		return siteInformation.getUrl() + "/feed/" + feed.getUrlWords();
	}
	
	public String getFeedsInboxUrl() {
		return siteInformation.getUrl() + "/feeds/inbox";
	}
	
	public String getFeedsUrl() {
		return siteInformation.getUrl() + "/feeds";
	}
	
	public String getTagUrl(Tag tag) {
		return siteInformation.getUrl() + "/" + tag.getName();
	}
	
	
	public String getAutoTagUrl(Tag tag) {
		return siteInformation.getUrl() + "/autotag/" + tag.getName();
	}

	public String getTagCombinerUrl(Tag firstTag, Tag secondTag) {
		return siteInformation.getUrl() + "/" + firstTag.getName() + "+" + secondTag.getName();
	}
	
	public String getTagSearchUrl(Tag tag, String keywords) {
		try {
			return siteInformation.getUrl() + "/search?keywords=" + URLEncoder.encode(keywords, "UTF-8") + "&tag="+ URLEncoder.encode(tag.getName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public String getLocalPageUrl(Newsitem newsitem) {
		return siteInformation.getUrl() + UrlWordsGenerator.markUrlForNewsitem(newsitem);
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
	
	
	public String getPublicTaggingUrl(Resource resource) {
		return siteInformation.getUrl() + "/tagging/tag?resource=" + resource.getId();
	}
	
	public String getTaggingUrl(Resource resource) {
		return siteInformation.getUrl() + "/edit/tagging?resource=" + resource.getId();
	}
	
	public String getArchiveUrl() {
		return siteInformation.getUrl() + "/archive";
	}
	
	public String getArchiveLinkUrl(Date date) {
		// TODO if this dateformatter thread safe? Replace with DateFormatter
		SimpleDateFormat df = new SimpleDateFormat();
	    df.applyPattern("yyyy");
	    String yearString = df.format(date.getTime());
	    df.applyPattern("MMM");
	    String monthString = df.format(date.getTime());
	    return siteInformation.getUrl() + "/archive/" + yearString + "/" + monthString.toLowerCase();		
	}

	public String getOpenIDCallbackUrl() {
		return siteInformation.getUrl() + "/openid/callback";
	}
	
	public String getProfileUrl(User user) {
		return siteInformation.getUrl() + "/profiles/" + user.getProfilename();
	}

	public String getWatchlistUrl() {
		return siteInformation.getUrl() + "/watchlist";
	}
	
}
