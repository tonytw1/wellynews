package nz.co.searchwellington.urls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendTag;
import nz.co.searchwellington.twitter.CachingTwitterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.dates.DateFormatter;
import uk.co.eelpieconsulting.common.geo.model.Place;

@Component
public class UrlBuilder {

	private SiteInformation siteInformation;
	private CachingTwitterService twitterService;	// TODO This is an odd injection
	private DateFormatter dateFormatter;
	private UrlWordsGenerator urlWordsGenerator;
	
	@Autowired
	public UrlBuilder(SiteInformation siteInformation, CachingTwitterService twitterService) {		
		this.siteInformation = siteInformation;
		this.twitterService = twitterService;
		this.dateFormatter = new DateFormatter();
		this.urlWordsGenerator = new UrlWordsGenerator();
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
	
	public String getTwitterReactionsUrl() {
		return siteInformation.getUrl() + "/twitter";
	}
	
	public String getFeedUrl(FrontendFeed feed) {
		return siteInformation.getUrl() + "/feed/" + feed.getUrlWords();
	}
	
	public String getFeedUrlFromFeedName(String feedname) {		
		return siteInformation.getUrl() + "/feed/" + urlWordsGenerator.makeUrlWordsFromName(feedname);
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
	
	public String getTagUrl(FrontendTag tag) {
		return siteInformation.getUrl() + "/" + tag.getId();
	}
	
	public String getAutoTagUrl(Tag tag) {
		return siteInformation.getUrl() + "/" + tag.getName() + "/autotag";
	}
	
	public String getTagCombinerUrl(Tag firstTag, FrontendTag secondTag) {
		return siteInformation.getUrl() + "/" + firstTag.getName() + "+" + secondTag.getId();
	}
	
	public String getTagCombinerUrl(Tag firstTag, Tag secondTag) {
		return siteInformation.getUrl() + "/" + firstTag.getName() + "+" + secondTag.getName();
	}
	
	public String getTagSearchUrl(Tag tag, String keywords) {
		return getTagUrl(tag) + "?keywords=" + urlEncode(keywords);
	}
	
	public String getLocalPageUrl(FrontendResource resource) {
		return siteInformation.getUrl() + resource.getUrlWords();
	}
	
	public String getPublisherUrl(String publisherName) {
		if (publisherName != null) {
			return siteInformation.getUrl() + "/" + urlWordsGenerator.makeUrlWordsFromName(publisherName);
		}
		return null;
	}

	public String getPublisherCombinerUrl(String publisherName, Tag tag) {
		return siteInformation.getUrl() + "/" + urlWordsGenerator.makeUrlWordsFromName(publisherName) + "+" + tag.getName();
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
	public String getTaggingUrl(FrontendResource resource) {
		return this.getLocalPageUrl(resource);
	}
	
	public String getArchiveUrl() {
		return siteInformation.getUrl() + "/archive";
	}
	
	public String getArchiveLinkUrl(Date date) {
		return siteInformation.getUrl() + "/archive/" + dateFormatter.yearMonthUrlStub(date);
	}
	
	public String getOpenIDCallbackUrl() {
		return siteInformation.getUrl() + "/openid/callback";
	}
	
	@Deprecated
	public String getProfileUrl(User user) {
		return siteInformation.getUrl() + "/profiles/" + user.getProfilename();
	}
	
	public String getProfileUrlFromProfileName(String username) {
		return siteInformation.getUrl() + "/profiles/" + urlEncode(username);
	}

	public String getWatchlistUrl() {
		return siteInformation.getUrl() + "/watchlist";
	}

	public String getTwitterCallbackUrl() {
		return siteInformation.getUrl() + "/twitter/callback";
	}

	public String getLocationUrlFor(Place somewhere) {
		if (somewhere.getOsmId() != null) {
			return siteInformation.getUrl() + "/geotagged?osm=" + urlEncode(somewhere.getOsmId().getId() + "/" + somewhere.getOsmId().getType());
		}
		return siteInformation.getUrl() + "/geotagged?location=" + urlEncode(somewhere.getAddress());
	}
	
	public String getSearchUrlFor(String keywords) {
		return siteInformation.getUrl() + "/search?keywords=" + urlEncode(keywords);
	}
	
	public String getTagSearchUrlFor(String keywords, Tag tag) {
		return getTagUrl(tag) + "?keywords=" + urlEncode(keywords);
	}
	
	public String getTwitterProfileImageUrlFor(String twitterUsername) {
		return twitterService.getTwitterProfileImageUrlFor(twitterUsername);
	}
	
	public String getSubmitWebsiteUrl() {
		return siteInformation.getUrl() + "/edit/submit/website";
	}
	
	public String getSubmitNewsitemUrl() {
		return siteInformation.getUrl() + "/edit/submit/newsitem";
	}
	
	public String getSubmitFeedUrl() {
		return siteInformation.getUrl() + "/edit/submit/feed";
	}
	
	private String urlEncode(String keywords) {
		try {
			return URLEncoder.encode(keywords, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public String getResourceUrl(FrontendResource resource) {
		return getLocalPageUrl(resource);		
	}
	
}
