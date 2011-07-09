package nz.co.searchwellington.urls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendWebsite;
import nz.co.searchwellington.model.frontend.FrontendWebsiteImpl;

public class UrlBuilder {

	private SiteInformation siteInformation;
	
	public UrlBuilder(SiteInformation siteInformation) {		
		this.siteInformation = siteInformation;
	}
	
	public String getHomeUrl() {
		return siteInformation.getUrl();
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
		
	public String getFeedUrl(FrontendFeed feed) {		
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
	
	public String getLocalPageUrl(FrontendNewsitem newsitem) {
		return siteInformation.getUrl() + UrlWordsGenerator.markUrlForNewsitem(newsitem);
	}
	
	public String getPublisherUrl(FrontendWebsite publisher) {
		return siteInformation.getUrl() + "/" + publisher.getUrlWords();
	}

	public String getPublisherCombinerUrl(FrontendWebsiteImpl frontendWebsite, Tag tag) {
		return siteInformation.getUrl() + "/" + frontendWebsite.getUrlWords() + "+" + tag.getName();
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
	
	public String getPublicTaggingSubmissionUrl(Resource resource) {
		return siteInformation.getUrl() + "/tagging/submit";
	}
	
	@Deprecated // TODO Inline
	public String getTaggingUrl(FrontendNewsitem newsitem) {
		return this.getLocalPageUrl(newsitem);
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

	public String getTwitterCallbackUrl() {
		return siteInformation.getUrl() + "/twitter/callback";
	}

	public String getLocationUrlFor(Geocode somewhere) {		
		return siteInformation.getUrl() + "/geotagged?location=" + URLEncoder.encode(somewhere.getAddress());
	}
	
}
